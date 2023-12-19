package dev.ssch.day18

import java.io.File
import java.lang.RuntimeException
import kotlin.math.abs

enum class Direction {
    Up, Right, Down, Left
}

fun parseDirectionFromCharacter(ch: Char): Direction {
    return when (ch) {
        'U' -> Direction.Up
        'R' -> Direction.Right
        'D' -> Direction.Down
        'L' -> Direction.Left
        else -> throw RuntimeException("Unknown direction $ch")
    }
}

data class Position(
    val row: Long,
    val col: Long,
) {

    fun moveBy(direction: Direction, amount: Long): Position {
        return when (direction) {
            Direction.Up -> Position(row - amount, col)
            Direction.Right -> Position(row, col + amount)
            Direction.Down -> Position(row + amount, col)
            Direction.Left -> Position(row, col - amount)
        }
    }

    fun get4Neighborhood(): List<Position> {
        return listOf(
            Position(row + 1, col),
            Position(row - 1, col),
            Position(row, col + 1),
            Position(row, col - 1),
        )
    }

    fun moveBy(deltaRow: Long, deltaCol: Long): Position {
        return Position(row + deltaRow, col + deltaCol)
    }
}

fun part1(file: File) {
    val instructions = file.readLines()

    data class Acc(
        val currentPosition: Position,
        val trenchPositions: Set<Position>,
    )

    val trenchPositions = instructions.fold(Acc(Position(0, 0), emptySet())) { acc, instruction ->
        val (direction, amount) = Regex("""([URDL]) ([0-9]+) \(#[a-f0-9]{6}\)""")
            .matchEntire(instruction)!!.destructured.let { (direction, amount) ->
                Pair(parseDirectionFromCharacter(direction.first()), amount.toLong())
            }

        val newPositions = (0..amount).map {
            acc.currentPosition.moveBy(direction, it)
        }

        Acc(
            newPositions.last(),
            acc.trenchPositions + newPositions
        )
    }.trenchPositions

    val minRow = trenchPositions.minOf { it.row }

    val startCubeForFloodFill = trenchPositions
        .filter { it.row == minRow }
        .map { it.moveBy(Direction.Down, 1) }
        .first { !trenchPositions.contains(it) }

    val visitedPositions = mutableSetOf<Position>()
    val queue = mutableListOf(startCubeForFloodFill)

    while (queue.isNotEmpty()) {
        val currentPosition = queue.removeAt(0)
        if (!visitedPositions.contains(currentPosition)) {
            visitedPositions.add(currentPosition)
            currentPosition.get4Neighborhood()
                .filter { !trenchPositions.contains(it) }
                .forEach { queue.add(it) }
        }
    }

    println(visitedPositions.size + trenchPositions.size)
}

// ---

fun parseDirectionFromNumber(number: Int): Direction {
    return when (number) {
        0 -> Direction.Right
        1 -> Direction.Down
        2 -> Direction.Left
        3 -> Direction.Up
        else -> throw RuntimeException("Unknown direction $number")
    }
}

fun calculateContourPositions(cornerPositions: List<Position>): List<Position> {
    /*
     *   +-+-+-+-+-+-+-+
     *   |#|#|#|#| | | |
     *   +-+-+-+-+-+-+-+
     *   |#| | |#|#|#|#|
     *   +-+-+-+-+-+-+-+
     *   |#|#| | | | |#|
     *   +-+-+-+-+-+-+-+
     *   | |#|#|#|#|#|#|
     *   +-+-+-+-+-+-+-+
     *
     *   outer positions (+) of border above
     *   +-------+
     *   |# # # #|
     *   |       +-----+
     *   |#     # # # #|
     *   |             |
     *   |# #         #|
     *   +-+           |
     *     |# # # # # #|
     *     +-----------+
     */
    val outerPositions = (cornerPositions.takeLast(1) + cornerPositions + cornerPositions.take(1))
        .windowed(3) { (prev, current, next) ->
            when {
                // convex corners
                // +->
                // |
                prev.row > current.row && current.col < next.col -> current
                // -+
                //  V
                prev.col < current.col && current.row < next.row -> current.moveBy(0, 1)
                //   |
                // <-+
                prev.row < current.row && current.col > next.col -> current.moveBy(1, 1)
                // ^
                // +-
                prev.col > current.col && current.row > next.row -> current.moveBy(1, 0)

                // concave corners
                //  ^
                // -+
                prev.col < current.col && current.row > next.row -> current
                // |
                // +->
                prev.row < current.row && current.col < next.col -> current.moveBy(0, 1)
                // +-
                // V
                prev.col > current.col && current.row < next.row -> current.moveBy(1, 1)
                // <-+
                //   |
                prev.row > current.row && current.col > next.col -> current.moveBy(1, 0)
                else -> throw RuntimeException("$prev $current $next")
            }
        }
    return outerPositions
}

fun part2(file: File) {
    val instructions = file.readLines()

    val cornerPositions = instructions.dropLast(1).runningFold(Position(0, 0)) { acc, instruction ->
        val (direction, amount) = Regex("""[URDL] [0-9]+ \(#([a-f0-9]{6})\)""")
            .matchEntire(instruction)!!.destructured.let { (color) ->
                Pair(parseDirectionFromNumber(color.last().digitToInt()), color.take(5).toLong(16))
            }
        acc.moveBy(direction, amount)
    }

    // inner and outer contour (support clockwise and counter-clockwise trenches)
    val contours = listOf(
        calculateContourPositions(cornerPositions),
        calculateContourPositions(cornerPositions.asReversed())
    )

    val area = contours.maxOf { contour ->
        abs(contour.windowed(2) { (start, end) ->
            ((end.col - start.col) * start.row)
        }.sum())
    }

    println(area)
}

fun main() {
    part1(File("inputs/18-part1.txt"))
    part1(File("inputs/18.txt"))
    println("---")
    part2(File("inputs/18-part1.txt"))
    part2(File("inputs/18.txt"))
}
