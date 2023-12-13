package dev.ssch.day13

import dev.ssch.day05.split
import java.io.File

fun List<String>.transposed(): List<String> {
    return this.first().indices.map { col ->
        this.map { it[col] }.joinToString("")
    }
}

data class MirrorPositions(
    val verticalMirrors: Set<Int>,
    val horizontalMirrors: Set<Int>,
)

fun findPossibleMirrorPositions(grid: List<String>): MirrorPositions { // (vertical, horizontal)
    fun List<String>.findVerticalMirrorPositions(): Set<Int> {
        return this.fold((1..<this.first().length).toList()) { possibleMirrors, gridLine ->
            possibleMirrors.filter { column ->
                val leftSide = gridLine.take(column)
                val rightSide = gridLine.drop(column)
                leftSide.reversed().zip(rightSide).all { it.first == it.second }
            }
        }.toSet()
    }

    val possibleVerticalMirrors = grid.findVerticalMirrorPositions()
    val possibleHorizontalMirrors = grid.transposed().findVerticalMirrorPositions()
    return MirrorPositions(possibleVerticalMirrors, possibleHorizontalMirrors)
}

fun part1(file: File) {
    val lines = file.readLines()

    val grids = lines.split { it.isEmpty() }
    val sum = grids
        .map { findPossibleMirrorPositions(it) }
        .flatMap { mirrors ->
            mirrors.verticalMirrors + mirrors.horizontalMirrors.map { it * 100 }
        }.sum()

    println(sum)
}

// ---

data class Position(
    val row: Int,
    val col: Int,
)

fun generate2DSequence(rows: Int, cols: Int): Sequence<Position> {
    return (0..<rows).asSequence().flatMap { row ->
        (0..<cols).asSequence().map { col -> Position(row, col) }
    }
}

fun List<String>.flippedAtPosition(position: Position): List<String> {
    return this.mapIndexed { row, line ->
        if (row == position.row) {
            if (line[position.col] == '#') {
                line.replaceRange(position.col, position.col + 1, ".")
            } else {
                line.replaceRange(position.col, position.col + 1, "#")
            }
        } else {
            line
        }
    }
}

fun extractAddedMirrorPositions(previousMirrors: MirrorPositions, newMirrors: MirrorPositions): MirrorPositions {
    return MirrorPositions(
        newMirrors.verticalMirrors.subtract(previousMirrors.verticalMirrors),
        newMirrors.horizontalMirrors.subtract(previousMirrors.horizontalMirrors),
    )
}

fun part2(file: File) {
    val lines = file.readLines()

    val grids = lines.split { it.isEmpty() }
    val sum = grids
        .map { grid ->
            val mirrorPositionsWithoutChange = findPossibleMirrorPositions(grid)
            generate2DSequence(grid.size, grid.first().length)
                .map {
                    findPossibleMirrorPositions(grid.flippedAtPosition(it))
                }.map {
                    extractAddedMirrorPositions(mirrorPositionsWithoutChange, it)
                }.filter {
                    it.verticalMirrors.isNotEmpty() || it.horizontalMirrors.isNotEmpty()
                }.first()
        }.flatMap { mirrors ->
            mirrors.verticalMirrors + mirrors.horizontalMirrors.map { it * 100 }
        }.sum()

    println(sum)
}

fun main() {
    part1(File("inputs/13-part1.txt"))
    part1(File("inputs/13.txt"))
    println("---")
    part2(File("inputs/13-part1.txt"))
    part2(File("inputs/13.txt"))
}
