package dev.ssch.day16

import java.io.File
import java.lang.RuntimeException

enum class Direction {
    Up, Right, Down, Left
}

data class Position(
    val row: Int,
    val col: Int,
)

data class Beam(
    val position: Position,
    val direction: Direction,
) {
    fun moveOneStep(newDirection: Direction = direction): Beam {
        return when (newDirection) {
            Direction.Up -> Beam(Position(position.row - 1, position.col), newDirection)
            Direction.Right -> Beam(Position(position.row, position.col + 1), newDirection)
            Direction.Down -> Beam(Position(position.row + 1, position.col), newDirection)
            Direction.Left -> Beam(Position(position.row, position.col - 1), newDirection)
        }
    }
}

fun moveBeam(beam: Beam, grid: List<String>): List<Beam> {
    return when (grid[beam.position.row][beam.position.col]) {
        '.' -> listOf(beam.moveOneStep())
        '/' -> when (beam.direction) {
            Direction.Up -> listOf(beam.moveOneStep(Direction.Right))
            Direction.Right -> listOf(beam.moveOneStep(Direction.Up))
            Direction.Down -> listOf(beam.moveOneStep(Direction.Left))
            Direction.Left -> listOf(beam.moveOneStep(Direction.Down))
        }
        '\\' -> when (beam.direction) {
            Direction.Up -> listOf(beam.moveOneStep(Direction.Left))
            Direction.Right -> listOf(beam.moveOneStep(Direction.Down))
            Direction.Down -> listOf(beam.moveOneStep(Direction.Right))
            Direction.Left -> listOf(beam.moveOneStep(Direction.Up))
        }
        '|' -> when (beam.direction) {
            Direction.Up, Direction.Down -> listOf(beam.moveOneStep())
            Direction.Left, Direction.Right -> listOf(beam.moveOneStep(Direction.Up), beam.moveOneStep(Direction.Down))
        }
        '-' -> when (beam.direction) {
            Direction.Up, Direction.Down -> listOf(beam.moveOneStep(Direction.Left), beam.moveOneStep(Direction.Right))
            Direction.Left, Direction.Right -> listOf(beam.moveOneStep())
        }
        else -> throw RuntimeException("unreachable")
    }.let { beams ->
        beams.filter {
            val row = it.position.row
            val col = it.position.col
            row >= 0 && row < grid.size && col >= 0 && col < grid[0].length
        }
    }
}

fun computeVisitedBeams(grid: List<String>, startBeam: Beam): Set<Beam> {
    data class Acc(
        val currentBeams: Set<Beam>,
        val visitedBeans: Set<Beam>
    )

    val visitedBeams = generateSequence { }
        .runningFold(Acc(setOf(startBeam), emptySet())) { acc, _ ->
            Acc(
                acc.currentBeams.flatMap { moveBeam(it, grid) }.toSet(),
                acc.visitedBeans + acc.currentBeams
            )
        }
        .windowed(2)
        .dropWhile { (a, b) -> a.visitedBeans != b.visitedBeans }
        .map { it[1] }
        .first().visitedBeans
    return visitedBeams
}

fun calculateNumberOfVisitedTiles(grid: List<String>, startBeam: Beam): Int {
    val visitedBeams = computeVisitedBeams(grid, startBeam)
    return visitedBeams.distinctBy { it.position }.size
}

fun part1(file: File) {
    val grid = file.readLines()
    val visitedTiles = calculateNumberOfVisitedTiles(grid, Beam(Position(0, 0), Direction.Right))
    println(visitedTiles)
}

// ---

fun part2(file: File) {
    val grid = file.readLines()

    val startPositions = grid.indices.map {
        Beam(Position(it, 0), Direction.Right)
    } + grid.indices.map {
        Beam(Position(it, grid[0].indices.last), Direction.Left)
    } + grid[0].indices.map {
        Beam(Position(0, it), Direction.Down)
    } + grid[0].indices.map {
        Beam(Position(grid.indices.last, it), Direction.Up)
    }

    val maximumNumberOfTiles = startPositions.maxOfOrNull {
        calculateNumberOfVisitedTiles(grid, it)
    }

    println(maximumNumberOfTiles)
}

fun main() {
    part1(File("inputs/16-part1.txt"))
    part1(File("inputs/16.txt"))
    println("---")
    part2(File("inputs/16-part1.txt"))
    part2(File("inputs/16.txt"))
}
