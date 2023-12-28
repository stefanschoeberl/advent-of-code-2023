package dev.ssch.day21

import java.io.File

data class Position(
    val row: Int,
    val col: Int,
) {
    fun get4Neighborhood(): List<Position> {
        return listOf(
            Position(row + 1, col),
            Position(row - 1, col),
            Position(row, col + 1),
            Position(row, col - 1),
        )
    }
}

fun getStartPosition(grid: List<String>): Position {
    val (index, value) = grid.asSequence().map {
        it.indexOf('S')
    }.withIndex().first { it.value != -1 }
    return Position(index, value)
}

fun List<String>.isGarden(position: Position): Boolean {
    return this[position.row][position.col].let {
        it == '.' || it == 'S'
    }
}

fun List<String>.isWithinBounds(position: Position): Boolean {
    return (position.row in this.indices) && (position.col in this.first().indices)
}

fun part1(file: File, steps: Int) {
    val grid = file.readLines()

    val startPosition = getStartPosition(grid)

    val finalPlots = (1..steps).fold(setOf(startPosition)) { acc, _ ->
        acc.flatMap { it.get4Neighborhood() }
            .filter { grid.isWithinBounds(it) }
            .filter { grid.isGarden(it) }
            .toSet()
    }
    println(finalPlots.size)
}

// ---

fun List<String>.isGardenOnInfiniteGrid(position: Position): Boolean {
    return this[Math.floorMod(position.row, this.size)][Math.floorMod(position.col, this.first().length)].let {
        it == '.' || it == 'S'
    }
}

fun assume(description: String, check: Boolean) {
    if (!check) {
        throw RuntimeException("Input does not fulfill assumption: $description")
    }
}

fun numberOfPlotsOnInfiniteGrid(grid: List<String>, steps: Int): Set<Position> {
    val startPosition = getStartPosition(grid)
    val finalPlots = (1..steps).fold(setOf(startPosition)) { acc, _ ->
        acc.flatMap { it.get4Neighborhood() }
            .filter { grid.isGardenOnInfiniteGrid(it) }
            .toSet()
    }

    return finalPlots
}

fun part2(file: File) {
    val steps = 26501365
    val grid = file.readLines()

    val startPosition = getStartPosition(grid)

    // check special properties of input (assumptions)
    assume("grid is a square", grid.size == grid.first().length)
    assume("empty vertical column at S", grid.indices.all { grid.isGarden(Position(it, startPosition.col)) })
    assume("empty horizontal row at S", grid.indices.all { grid.isGarden(Position(startPosition.row, it)) })
    assume("empty border",
        grid.indices.all { grid.isGarden(Position(grid.indices.first, it)) }
                && grid.indices.all { grid.isGarden(Position(grid.indices.last, it)) }
                && grid.indices.all { grid.isGarden(Position(it, grid.indices.first)) }
                && grid.indices.all { grid.isGarden(Position(it, grid.indices.last)) }
    )
    assume("given steps == size * n + size / 2", steps % grid.size == grid.size / 2)

    val plotsAfterIteration = (1..2).map {
        val stepsForIterations = grid.size * it + grid.size / 2
        numberOfPlotsOnInfiniteGrid(grid, stepsForIterations)
    }

    val (fullEvenPlots, fullOddPlots) =  plotsAfterIteration
        .map { plots -> plots.filter { grid.isWithinBounds(it) }.size.toLong() }


    val (topPlots, bottomPlots, leftPlots, rightPlots) = sequenceOf<(Position) -> Boolean>(
        { pos -> pos.row < grid.indices.first && pos.col in grid.indices },
        { pos -> pos.row > grid.indices.last && pos.col in grid.indices },
        { pos -> pos.col < grid.indices.first && pos.row in grid.indices },
        { pos -> pos.col > grid.indices.last && pos.row in grid.indices },
    ).map { pred -> plotsAfterIteration.first().count { pred(it) }.toLong() }.toList()

    val cornerRegionsPredicates = sequenceOf<(Position) -> Boolean>(
        { pos -> pos.row < grid.indices.first && pos.col < grid.indices.first }, // top left
        { pos -> pos.row > grid.indices.last && pos.col < grid.indices.first },  // bottom left
        { pos -> pos.row < grid.indices.first && pos.col > grid.indices.last },  // top right
        { pos -> pos.row > grid.indices.last && pos.col > grid.indices.last },   // bottom right
    )
    val (topLeftPlots, bottomLeftPlots, topRightPlots, bottomRightPlots) = cornerRegionsPredicates
        .map { pred -> plotsAfterIteration.first().count { pred(it) }.toLong() }.toList()

    val (biggerTopLeftPlots, biggerBottomLeftPlots, biggerTopRightPlots, biggerBottomRightPlots) = cornerRegionsPredicates
        .map { pred -> plotsAfterIteration[1].count { pred(it) }.toLong() }
        .zip(sequenceOf(topLeftPlots, bottomLeftPlots, topRightPlots, bottomRightPlots))
        .map {
            it.first - 2 * it.second
        }.toList()

    fun numberOfPlots(iterations: Long): Long {
        return (iterations * iterations * fullEvenPlots) +
                (iterations - 1) * (iterations - 1) * fullOddPlots +
                iterations * (topLeftPlots + bottomLeftPlots + topRightPlots + bottomRightPlots) +
                (iterations - 1) * (biggerTopLeftPlots + biggerBottomLeftPlots + biggerTopRightPlots + biggerBottomRightPlots) +
                (topPlots + bottomPlots + leftPlots + rightPlots)
    }

    println(numberOfPlots(steps.toLong() / grid.size))
}

fun main() {
    part1(File("inputs/21-part1.txt"), 6)
    part1(File("inputs/21.txt"), 64)
    println("---")
    part2(File("inputs/21.txt"))
}
