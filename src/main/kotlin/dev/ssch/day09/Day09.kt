package dev.ssch.day09

import java.io.File

fun calculateNextNumber(sequence: List<Int>): Int {
    val deltas = sequence.windowed(2).map { (a, b) -> b - a }
    return if (deltas.all { it == 0 }) {
        sequence.last()
    } else {
        sequence.last() + calculateNextNumber(deltas)
    }
}

fun part1(file: File) {
    val lines = file.readLines()

    val sum = lines.sumOf { line ->
        val sequence = line.split(" ").map { it.toInt() }
        calculateNextNumber(sequence)
    }

    println(sum)
}

// ---

fun calculatePreviousNumber(sequence: List<Int>): Int {
    val deltas = sequence.windowed(2).map { (a, b) -> b - a }
    return if (deltas.all { it == 0 }) {
        sequence.first()
    } else {
        sequence.first() - calculatePreviousNumber(deltas)
    }
}

fun part2(file: File) {
    val lines = file.readLines()

    val sum = lines.sumOf { line ->
        val sequence = line.split(" ").map { it.toInt() }
        calculatePreviousNumber(sequence)
    }

    println(sum)
}


fun main() {
    part1(File("inputs/09-part1.txt"))
    part1(File("inputs/09.txt"))
    println("---")
    part2(File("inputs/09-part1.txt"))
    part2(File("inputs/09.txt"))
}
