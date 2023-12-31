package dev.ssch.day22

import java.io.File
import kotlin.math.max
import kotlin.math.min

data class Point(
    val x: Int,
    val y: Int,
    val z: Int,
)

data class Brick(
    val start: Point,
    val end: Point,
) {
    fun moveUp(): Brick {
        return Brick(Point(start.x, start.y, start.z + 1), Point(end.x, end.y, end.z + 1))
    }

    fun moveDown(): Brick {
        return Brick(Point(start.x, start.y, start.z - 1), Point(end.x, end.y, end.z - 1))
    }

    val minX = min(this.start.x, this.end.x)
    val minY = min(this.start.y, this.end.y)
    val minZ = min(this.start.z, this.end.z)

    val maxX = max(this.start.x, this.end.x)
    val maxY = max(this.start.y, this.end.y)
    val maxZ = max(this.start.z, this.end.z)

    val sameX = this.minX == this.maxX
    val sameY = this.minY == this.maxY
    val sameZ = this.minZ == this.maxZ

    val xRange = this.minX..this.maxX
    val yRange = this.minY..this.maxY
    val zRange = this.minZ..this.maxZ

    val pointingX = this.sameY && this.sameZ
    val pointingY = this.sameX && this.sameZ
    val pointingZ = this.sameX && this.sameY
}

fun parseBrick(line: String): Brick {
    val (start, end) = line.split('~').map {
        val (x, y, z) = it.split(',').map { it.toInt() }
        Point(x, y, z)
    }
    return Brick(start, end)
}

fun Brick.intersectsOtherBrick(bricks: List<Brick>): Boolean {
    return this.findIntersectedBricks(bricks).any()
}

fun isPointOnBrick(point: Point, brick: Brick): Boolean {
    return (brick.pointingZ && point.x == brick.minX && point.y == brick.minY && point.z in brick.zRange) ||
            (brick.pointingX && point.y == brick.minY && point.z == brick.minZ && point.x in brick.xRange) ||
            (brick.pointingY && point.z == brick.minZ && point.x == brick.minX && point.y in brick.yRange)
}

fun Brick.intersects(other: Brick): Boolean {
    return (this.pointingZ && ((other.pointingX && this.minY == other.minY && this.minX in other.xRange && other.minZ in this.zRange)
            || (other.pointingY && this.minX == other.minX && this.minY in other.yRange && other.minZ in this.zRange))) ||

            (this.pointingX && ((other.pointingY && this.minZ == other.minZ && this.minY in other.yRange && other.minX in this.xRange)
                    || (other.pointingZ && this.minY == other.minY && this.minZ in other.zRange && other.minX in this.xRange))) ||

            (this.pointingY && ((other.pointingZ && this.minX == other.minX && this.minZ in other.zRange && other.minY in this.yRange)
                    || (other.pointingX && this.minZ == other.minZ && this.minX in other.xRange && other.minY in this.yRange)))
}

fun Brick.findIntersectedBricks(bricks: List<Brick>): Sequence<Brick> {
    return bricks.asSequence().filter { brick ->
        sequenceOf(this.start, this.end)
            .any { point -> isPointOnBrick(point, brick) } ||
                sequenceOf(brick.start, brick.end).any { point -> isPointOnBrick(point, this) } ||
                this.intersects(brick)
    }
}

fun settleBricks(bricks: List<Brick>): List<Brick> {
    data class Acc(
        val bricksToProcess: List<Brick>,
        val settledBricks: List<Brick>,
    )

    val (bricksToProcess, settledBricks) = bricks.partition { it.minZ > 1 }

    return generateSequence(
        Acc(
            bricksToProcess.sortedBy { it.minZ },
            settledBricks
        )
    ) { (bricksToProcess, settledBricks) ->
        val currentBrick = bricksToProcess.first()
        val movedBrick = generateSequence(currentBrick) { it.moveDown() }
            .takeWhile { it.minZ > 0 && !it.intersectsOtherBrick(settledBricks) }.last()
        Acc(
            bricksToProcess.drop(1),
            settledBricks + movedBrick
        )
    }.first { it.bricksToProcess.isEmpty() }.settledBricks
}

data class BrickDependencies(
    val supportedBy: Map<Brick, Set<Brick>>,
    val supports: Map<Brick, Set<Brick>>,
)

fun computeBrickDependencies(settledBricks: List<Brick>): BrickDependencies {
    val supportedBy = settledBricks.associateWith { currentBrick ->
        val remainingBricks = settledBricks - currentBrick
        currentBrick.moveDown().findIntersectedBricks(remainingBricks).toSet()
    }
    val supports = settledBricks.associateWith { currentBrick ->
        val remainingBricks = settledBricks - currentBrick
        currentBrick.moveUp().findIntersectedBricks(remainingBricks).toSet()
    }
    return BrickDependencies(supportedBy, supports)
}

fun part1(file: File) {
    val lines = file.readLines()
    val initialBricks = lines.map { parseBrick(it) }
    val settledBricks = settleBricks(initialBricks)

    val (supportedBy, supports) = computeBrickDependencies(settledBricks)

    val numberOfRemovableBricks = settledBricks.count { currentBrick ->
        val supportedBricks = supports[currentBrick]!!
        supportedBricks.all { supportedBy[it]!!.size > 1 }
    }

    println(numberOfRemovableBricks)
}

// ---

fun numberOfOtherFallenBricks(brickToRemove: Brick, brickDependencies: BrickDependencies): Int {
    data class Acc(
        val bricksToRemove: Set<Brick>,
        val removedBricks: Set<Brick>,
    )

    return generateSequence(Acc(setOf(brickToRemove), emptySet())) { (bricksToRemove, removedBricks) ->
        val allRemovedBricks = removedBricks + bricksToRemove
        val bricksToRemoveInNextRound = bricksToRemove
            .flatMap { brickDependencies.supports[it]!! }
            .filter {
                (brickDependencies.supportedBy[it]!! - allRemovedBricks).isEmpty()
            }.toSet()
        Acc(bricksToRemoveInNextRound, allRemovedBricks)
    }.first { it.bricksToRemove.isEmpty() }.removedBricks.size - 1
}

fun part2(file: File) {
    val lines = file.readLines()
    val initialBricks = lines.map { parseBrick(it) }
    val settledBricks = settleBricks(initialBricks)

    val brickDependencies = computeBrickDependencies(settledBricks)

    val sum = settledBricks.sumOf { numberOfOtherFallenBricks(it, brickDependencies) }

    println(sum)
}

fun main() {
    part1(File("inputs/22-part1.txt"))
    part1(File("inputs/22.txt"))
    println("---")
    part2(File("inputs/22-part1.txt"))
    part2(File("inputs/22.txt"))
}
