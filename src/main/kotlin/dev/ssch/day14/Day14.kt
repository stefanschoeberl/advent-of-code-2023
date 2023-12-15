package dev.ssch.day14

import dev.ssch.day13.transposed
import java.io.File
import java.lang.RuntimeException

fun rollToTheLeft(line: String): String {
    return line.indices.fold(line) { acc, i ->
        when (acc[i]) {
            'O', '#' -> acc // keep unchanged
            '.' -> acc.drop(i).takeWhile { it != '#' }.indexOf('O').let { // pull next stone
//                println("replace $i with ${i + it}")
                if (it != -1) {
                    acc
                        .replaceRange(i, i + 1, "O")
                        .replaceRange(i + it, i + it + 1, ".")
                } else {
                    acc
                }
            }

            else -> throw RuntimeException("unreachable")
        }
    }
}

fun calculateLoad(grid: List<String>): Int {
    return grid.sumOf {
        it.reversed().mapIndexed { index, c ->
            if (c == 'O') {
                index + 1
            } else {
                0
            }
        }.sum()
    }
}

fun part1(file: File) {
    val grid = file.readLines()

    val gridAfterRolling = grid.transposed().map { rollToTheLeft(it) }

    println(calculateLoad(gridAfterRolling))
}

// ---

fun List<String>.rotateClockwise(): List<String> {
    /*
    * 12
    * 34
    * transpose
    * 13
    * 24
    * mirror left <-> right
    * 31
    * 42
    * */
    return this.transposed().map { it.reversed() }
}

fun List<String>.rotateCounterClockwise(): List<String> {
    /*
    * 12
    * 34
    * mirror left <-> right
    * 21
    * 43
    * transpose
    * 24
    * 13
    * */
    return this.map { it.reversed() }.transposed()
}

fun performCycle(grid: List<String>): List<String> {
    return grid
        .map { rollToTheLeft(it) }.rotateClockwise()
        .map { rollToTheLeft(it) }.rotateClockwise()
        .map { rollToTheLeft(it) }.rotateClockwise()
        .map { rollToTheLeft(it) }.rotateClockwise()
}

fun part2(file: File) {
    val grid = file.readLines()

    val totalCycles = 1000000000

    var cycleIndex = 0
    val visitedGrids = mutableMapOf<List<String>, Int>()
    var currentGrid = grid.rotateCounterClockwise()
    while (!visitedGrids.contains(currentGrid)) {
        visitedGrids[currentGrid] = cycleIndex
        currentGrid = performCycle(currentGrid)
        cycleIndex++
    }

    val cycleLength = cycleIndex - visitedGrids[currentGrid]!!
    val missingCyclesForTotal = (totalCycles - visitedGrids[currentGrid]!!) % cycleLength

    for (i in 0..<missingCyclesForTotal) {
        currentGrid = performCycle(currentGrid)
    }

    println(calculateLoad(currentGrid))
}

fun main() {
    part1(File("inputs/14-part1.txt"))
    part1(File("inputs/14.txt"))
    println("---")
    part2(File("inputs/14-part1.txt"))
    part2(File("inputs/14.txt"))
}
