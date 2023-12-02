package dev.ssch.day02

import java.io.File

data class Game(
    val id: Int,
    val rounds: List<Map<String, Int>>
)

fun parseLineToGame(line: String): Game {
    val (gameText, roundsText) = line.split(":")
    val gameId = gameText.split(" ")[1].toInt()

    val rounds = roundsText.trim().split(";").map { round ->
        round.trim().split(",").associate { entry ->
            val (amount, color) = entry.trim().split(" ")
            color to amount.toInt()
        }
    }

    return Game(gameId, rounds)
}

fun part1(file: File) {
    val sum = file.readLines().map {
        parseLineToGame(it)
    }.filter { game ->
        game.rounds.all { it.getOrDefault("red", 0) <= 12 } &&
        game.rounds.all { it.getOrDefault("green", 0) <= 13 } &&
        game.rounds.all { it.getOrDefault("blue", 0) <= 14 }
    }.sumOf { it.id }
    println(sum)
}

// ---

fun part2(file: File) {
    val sum = file.readLines().map {
        parseLineToGame(it)
    }.sumOf { game ->
        val reds = game.rounds.maxOf { it.getOrDefault("red", 0) }
        val greens = game.rounds.maxOf { it.getOrDefault("green", 0) }
        val blues = game.rounds.maxOf { it.getOrDefault("blue", 0) }
        reds * greens * blues
    }
    println(sum)
}

fun main() {
    part1(File("inputs/02-part1.txt"))
    part1(File("inputs/02.txt"))
    println("---")
    part2(File("inputs/02-part1.txt"))
    part2(File("inputs/02.txt"))
}
