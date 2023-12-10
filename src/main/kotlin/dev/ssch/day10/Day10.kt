package dev.ssch.day10

import java.io.File
import java.lang.RuntimeException

data class Position(
    val row: Int,
    val col: Int,
) {
    fun moveNorth(): Position {
        return Position(row - 1, col)
    }

    fun moveEast(): Position {
        return Position(row, col + 1)
    }

    fun moveSouth(): Position {
        return Position(row + 1, col)
    }

    fun moveWest(): Position {
        return Position(row, col - 1)
    }
}

enum class Connection {
    North,
    East,
    South,
    West,
}

fun Char.getConnections(): Set<Connection> {
    return when (this) {
        '|' -> setOf(Connection.North, Connection.South)
        '-' -> setOf(Connection.East, Connection.West)
        'L' -> setOf(Connection.North, Connection.East)
        'J' -> setOf(Connection.North, Connection.West)
        '7' -> setOf(Connection.South, Connection.West)
        'F' -> setOf(Connection.South, Connection.East)
        'S' -> setOf(Connection.North, Connection.East, Connection.South, Connection.West)
        else -> emptySet()
    }
}

fun getTile(grid: List<String>, position: Position): Char {
    return grid.getOrNull(position.row)?.getOrNull(position.col) ?: '.'
}

fun expandPosition(grid: List<String>, position: Position): List<Position> {
    val currentConnections = getTile(grid, position).getConnections()
    return currentConnections.mapNotNull {
        val nextPosition = when (it) {
            Connection.North -> position.moveNorth()
            Connection.East -> position.moveEast()
            Connection.South -> position.moveSouth()
            Connection.West -> position.moveWest()
        }
        val nextTile = getTile(grid, nextPosition)
        val connects = when (it) {
            Connection.North -> nextTile.getConnections().contains(Connection.South)
            Connection.East -> nextTile.getConnections().contains(Connection.West)
            Connection.South -> nextTile.getConnections().contains(Connection.North)
            Connection.West -> nextTile.getConnections().contains(Connection.East)
        }
        if (connects) {
            nextPosition
        } else {
            null
        }
    }
}

fun part1(file: File) {
    val grid = file.readLines()

    val startPosition = grid.asSequence().mapIndexed { row, s ->
        val col = s.indexOf('S')
        if (col != -1) {
            Position(row, col)
        } else {
            null
        }
    }.filter { it != null }.first()!!

    data class Acc(
        val visitedPositions: Set<Position>,
        val openTiles: Set<Position>,
        val steps: Int
    )

    val steps = generateSequence(0) { it + 1 }
        .runningFold(Acc(emptySet(), setOf(startPosition), 0)) { acc, i ->
            val nextTiles = acc.openTiles.flatMap {
                expandPosition(grid, it)
            }.filter { !acc.visitedPositions.contains(it) }.toSet()

            Acc(
                acc.visitedPositions + nextTiles,
                nextTiles,
                acc.steps + 1
            )
        }.takeWhile { it.openTiles.isNotEmpty() }.last().steps
    println(steps)
}

// ---

fun computeActualStartTile(grid: List<String>, startPosition: Position): Char {
    val actualConnections = sequenceOf(
        Connection.North,
        Connection.East,
        Connection.West,
        Connection.South
    ).filter {
        when (it) {
            Connection.North -> getTile(grid, startPosition.moveNorth()).getConnections().contains(Connection.South)
            Connection.East -> getTile(grid, startPosition.moveEast()).getConnections().contains(Connection.West)
            Connection.South -> getTile(grid, startPosition.moveSouth()).getConnections().contains(Connection.North)
            Connection.West -> getTile(grid, startPosition.moveWest()).getConnections().contains(Connection.East)
        }
    }.toSet()

    return if (actualConnections.contains(Connection.North) && actualConnections.contains(Connection.South)) {
        '|'
    } else if (actualConnections.contains(Connection.West) && actualConnections.contains(Connection.East)) {
        '-'
    } else if (actualConnections.contains(Connection.North) && actualConnections.contains(Connection.West)) {
        'J'
    } else if (actualConnections.contains(Connection.South) && actualConnections.contains(Connection.East)) {
        'F'
    } else if (actualConnections.contains(Connection.South) && actualConnections.contains(Connection.West)) {
        '7'
    } else if (actualConnections.contains(Connection.North) && actualConnections.contains(Connection.East)) {
        'L'
    } else {
        throw RuntimeException("impossible start tile encountered")
    }
}

fun part2(file: File) {
    val grid = file.readLines()

    val startPosition = grid.asSequence().mapIndexed { row, s ->
        val col = s.indexOf('S')
        if (col != -1) {
            Position(row, col)
        } else {
            null
        }
    }.filter { it != null }.first()!!

    data class Acc(
        val visitedPositions: Set<Position>,
        val openTiles: Set<Position>,
        val steps: Int
    )

    val loopPositions = generateSequence(0) { it + 1 }
        .runningFold(Acc(emptySet(), setOf(startPosition), 0)) { acc, i ->
            val nextTiles = acc.openTiles.flatMap {
                expandPosition(grid, it)
            }.filter { !acc.visitedPositions.contains(it) }.toSet()

            Acc(
                acc.visitedPositions + nextTiles,
                nextTiles,
                acc.steps + 1
            )
        }.takeWhile { it.openTiles.isNotEmpty() }.last().visitedPositions

    val tilesInsideLoop = grid.mapIndexed { row, s ->
        data class Acc(
            val previousInLoop: Boolean,
            val horizontalSegmentStarter: Char?,
            val counter: Int,
        )
        s.map {
            if (it == 'S') {
                computeActualStartTile(grid, startPosition)
            } else {
                it
            }
        }.runningFoldIndexed(Acc(false, null, 0)) { col, acc, currentTile ->
            val currentPosition = Position(row, col)

            if (loopPositions.contains(currentPosition)) {
                if (currentTile == '|') {
                    // vertical wall crossed => swap inside/outside
                    Acc(!acc.previousInLoop, null, acc.counter)
                } else if (currentTile in setOf('L', '7', 'J', 'F')) {
                    if (acc.horizontalSegmentStarter == null) {
                        // start horizontal segment
                        Acc(acc.previousInLoop, currentTile, acc.counter)
                    } else {
                        // end horizontal segment
                        if ((acc.horizontalSegmentStarter == 'L' && currentTile == 'J') || (acc.horizontalSegmentStarter == 'F' && currentTile == '7')) {
                            // (L---J or F---7) ended => inside/outside does not change
                            Acc(acc.previousInLoop, null, acc.counter)
                        } else {
                            //  (F---J or L---7) ended => inside/outside changes
                            Acc(!acc.previousInLoop, null, acc.counter)
                        }
                    }
                } else {
                    // '-' wall => don't update anything
                    acc
                }
            } else if (acc.previousInLoop) {
                // non-border tile within loop => increment counter
                Acc(acc.previousInLoop, acc.horizontalSegmentStarter, acc.counter + 1)
            } else {
                // non-border tile outside loop => don't update anything
                acc
            }

        }.last().counter
    }.sum()

    println(tilesInsideLoop)
}

fun main() {
    part1(File("inputs/10-part1-1.txt"))
    part1(File("inputs/10-part1-2.txt"))
    part1(File("inputs/10.txt"))
    println("---")
    part2(File("inputs/10-part2-1.txt"))
    part2(File("inputs/10-part2-2.txt"))
    part2(File("inputs/10-part2-3.txt"))
    part2(File("inputs/10.txt"))
}
