package dev.ssch.day03

import java.io.File

data class Coordinate(
    val row: Int,
    val col: Int,
) {
    fun get8Neighbourhood(): List<Coordinate> {
        return listOf(
            Coordinate(row - 1, col - 1),
            Coordinate(row, col - 1),
            Coordinate(row + 1, col - 1),
            Coordinate(row - 1, col),
            Coordinate(row + 1, col),
            Coordinate(row - 1, col + 1),
            Coordinate(row, col + 1),
            Coordinate(row + 1, col + 1),
        )
    }
}

fun List<String>.getAt(row: Int, col: Int): Char {
    return this[row][col]
}

fun List<String>.getAt(coordinate: Coordinate): Char {
    return this.getAt(coordinate.row, coordinate.col)
}

fun List<String>.isWithinBounds(coordinate: Coordinate): Boolean {
    return coordinate.row >= 0 && coordinate.col >= 0 && coordinate.row < this.size && coordinate.col < this[0].length
}

fun part1(file: File) {
    val schematic = file.readLines()

    data class Acc(
        val partNumbers: List<Int>,
        val numberBuffer: Int,
        val isPartNumber: Boolean
    )

    val sumOfPartNumbers = schematic.flatMapIndexed { row: Int, line: String ->
        // add . to the end of the line in case a part number is at the end of the line
        "$line.".foldIndexed(
            Acc(
                emptyList(),
                0,
                false
            )
        ) { col: Int, acc: Acc, currentValue: Char ->
            if (currentValue.isDigit()) {
                // add digit to number buffer, check if it is a part number
                val isPartNumber = acc.isPartNumber || Coordinate(row, col)
                    .get8Neighbourhood()
                    .filter { schematic.isWithinBounds(it) }
                    .any { !schematic.getAt(it).isDigit() && schematic.getAt(it) != '.' }
                val numberBuffer = acc.numberBuffer * 10 + currentValue.digitToInt()
                Acc(acc.partNumbers, numberBuffer, isPartNumber)
            } else if (acc.isPartNumber) {
                // save current number if it is a part number
                val partNumbers = acc.partNumbers + acc.numberBuffer
                Acc(partNumbers, 0, false)
            } else {
                // discard current number because it's not a part number
                Acc(acc.partNumbers, 0, false)
            }
        }.partNumbers
    }.sum()
    println(sumOfPartNumbers)
}

// ---

fun part2(file: File) {
    val schematic = file.readLines()

    data class PartNumberWithGear(
        val partNumber: Int,
        val gearCoordinate: Coordinate,
    )
    data class Acc(
        val partNumbersWithGears: List<PartNumberWithGear>,
        val numberBuffer: Int,
        val gearCoordinates: Set<Coordinate>
    )

    val sumOfGearRatios = schematic.flatMapIndexed { row: Int, line: String ->
        // add . to the end of the line in case a part number is at the end of the line
        "$line.".foldIndexed(
            Acc(
                emptyList(),
                0,
                emptySet()
            )
        ) { col: Int, acc: Acc, currentValue: Char ->
            if (currentValue.isDigit()) {
                // add digit to number buffer, search for adjacent gears adjacent
                val gearCoordinates = Coordinate(row, col)
                    .get8Neighbourhood()
                    .filter { schematic.isWithinBounds(it) }
                    .filter { schematic.getAt(it) == '*' }
                    .union(acc.gearCoordinates)
                val numberBuffer = acc.numberBuffer * 10 + currentValue.digitToInt()
                Acc(acc.partNumbersWithGears, numberBuffer, gearCoordinates)
            } else if (acc.gearCoordinates.isNotEmpty()) {
                // save current number if it is adjacent to a gear
                val partNumbersWithGears = acc.partNumbersWithGears + acc.gearCoordinates.map {
                    PartNumberWithGear(acc.numberBuffer, it)
                }
                Acc(partNumbersWithGears, 0, emptySet())
            } else {
                // discard current number because it's not adjacent to a gear
                Acc(acc.partNumbersWithGears, 0, emptySet())
            }
        }.partNumbersWithGears
    }
        .groupBy({ it.gearCoordinate }, { it.partNumber })
        .filter { it.value.size == 2 }
        .values
        .sumOf { it[0] * it[1] }

    println(sumOfGearRatios)
}

fun main() {
    part1(File("inputs/03-part1.txt"))
    part1(File("inputs/03.txt"))
    println("---")
    part2(File("inputs/03-part1.txt"))
    part2(File("inputs/03.txt"))
}
