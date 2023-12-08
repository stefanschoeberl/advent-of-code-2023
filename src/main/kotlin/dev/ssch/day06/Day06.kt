package dev.ssch.day06

import java.io.File

fun part1(file: File) {
    val lines = file.readLines()

    val (times, distances) = lines.map {
        it.split(" ").mapNotNull { it.toIntOrNull() }
    }

    val product = times.zip(distances)
        .map { (time, distance) ->
            (0..time)
                .map { buttonTime ->
                    val speed = buttonTime
                    val remainingTime = time - buttonTime
                    speed * remainingTime
                }.count {
                    it > distance
                }
        }.reduce { a, b -> a * b }
    println(product)
}

// ---

fun part2(file: File) {
    val lines = file.readLines()

    val (time, distance) = lines.map {
        it.split(":")[1].replace(" ", "").toLong()
    }

    val possibleWaysToWin = (0..time)
        .map { buttonTime ->
            val speed = buttonTime
            val remainingTime = time - buttonTime
            speed * remainingTime
        }.count {
            it > distance
        }

    println(possibleWaysToWin)
}

fun main() {
    part1(File("inputs/06-part1.txt"))
    part1(File("inputs/06.txt"))
    println("---")
    part2(File("inputs/06-part1.txt"))
    part2(File("inputs/06.txt"))
}
