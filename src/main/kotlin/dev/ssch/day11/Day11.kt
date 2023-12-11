package dev.ssch.day11

import java.io.File
import kotlin.math.abs

data class Position(
    val row: Int,
    val col: Int,
)

fun findGalaxyPositions(grid: List<String>): List<Position> {
    return grid.flatMapIndexed { row: Int, line: String ->
        line.mapIndexedNotNull { col, c ->
            if (c == '#') {
                Position(row, col)
            } else {
                null
            }
        }
    }
}


fun findEmptyRowsAndColumns(grid: List<String>): Pair<List<Int>, List<Int>> {
    val emptyRows = grid.mapIndexedNotNull { row, s ->
        if (!s.contains("#")) {
            row
        } else {
            null
        }
    }

    val emptyCols = (0..<grid.first().length).filter { col ->
        grid.none { it[col] == '#' }
    }
    return Pair(emptyRows, emptyCols)
}

fun computeTotalDistanceBetweenGalaxyPairs(actualGalaxyPositions: List<Position>): Long {
    val galaxyPairs = actualGalaxyPositions.flatMapIndexed { index: Int, first: Position ->
        actualGalaxyPositions.drop(index + 1).map { second ->
            Pair(first, second)
        }
    }

    return galaxyPairs.sumOf {
        abs(it.first.row - it.second.row).toLong() + abs(it.first.col - it.second.col).toLong()
    }
}

fun part1(file: File) {
    val grid = file.readLines()

    val galaxyPositions = findGalaxyPositions(grid)

    val (emptyRows, emptyCols) = findEmptyRowsAndColumns(grid)

    val actualGalaxyPositions = galaxyPositions
        .map { position ->
            Position(
                position.row + emptyRows.count { it < position.row },
                position.col + emptyCols.count { it < position.col },
            )
        }

    val totalDistance = computeTotalDistanceBetweenGalaxyPairs(actualGalaxyPositions)

    println(totalDistance)
}

// ---

fun part2(file: File) {
    val grid = file.readLines()

    val galaxyPositions = findGalaxyPositions(grid)

    val (emptyRows, emptyCols) = findEmptyRowsAndColumns(grid)

    val expansionRate = 1_000_000
    val actualGalaxyPositions = galaxyPositions
        .map { position ->
            Position(
                position.row + emptyRows.count { it < position.row } * (expansionRate - 1),
                position.col + emptyCols.count { it < position.col } * (expansionRate - 1),
            )
        }

    val totalDistance = computeTotalDistanceBetweenGalaxyPairs(actualGalaxyPositions)

    println(totalDistance)
}

fun main() {
    part1(File("inputs/11-part1.txt"))
    part1(File("inputs/11.txt"))
    println("---")
    part2(File("inputs/11-part1.txt"))
    part2(File("inputs/11.txt"))
}
