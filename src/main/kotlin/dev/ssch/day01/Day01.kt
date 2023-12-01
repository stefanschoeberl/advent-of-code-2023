package dev.ssch.day01

import java.io.File

fun part1(file: File) {
    val lines = file.readLines()
    val sum = lines.sumOf { line ->
        val digits = line.filter { it.isDigit() }
        digits.first().digitToInt() * 10 + digits.last().digitToInt()
    }
    println(sum)
}

// ---

fun extractNumber(line: String): Int {
    val pattern = Regex("one|two|three|four|five|six|seven|eight|nine|[0-9]")
    val firstMatch = pattern.find(line)!!
    val lastMatch = ((line.length - 1) downTo 0)
        .map { line.substring(it) }
        .firstNotNullOf { pattern.find(it) }

    fun textToInt(text: String): Int {
        return when (text) {
            "one" -> 1
            "two" -> 2
            "three" -> 3
            "four" -> 4
            "five" -> 5
            "six" -> 6
            "seven" -> 7
            "eight" -> 8
            "nine" -> 9
            else -> text.toInt()
        }
    }

    return textToInt(firstMatch.value) * 10 + textToInt(lastMatch.value)
}

fun part2(file: File) {
    val lines = file.readLines()
    val sum = lines.sumOf { line -> extractNumber(line) }
    println(sum)
}

fun main() {
    part1(File("inputs/01-part1.txt"))
    part1(File("inputs/01.txt"))
    println("---")
    part2(File("inputs/01-part2.txt"))
    part2(File("inputs/01.txt"))
}
