package dev.ssch.day04

import java.io.File
import kotlin.math.pow

fun part1(file: File) {
    val points = file
        .readLines()
        .map {
            it.split(Regex("[:|]"))
                .drop(1)
                .map { it.trim().split(" ").filter { it.isNotEmpty() }.map { it.toInt() } }
        }.sumOf {
            val amountMatching = it[0].intersect(it[1]).size
            if (amountMatching > 0) {
                2.0.pow((amountMatching - 1).toDouble()).toInt()
            } else {
                0
            }
        }
    println(points)
}

// ---

fun part2(file: File) {
    val amountOfCard = mutableMapOf<Int, Int>()

    val totalAmountOfCards = file
        .readLines()
        .map {
            it.split(Regex("[:|]"))
                .drop(1)
                .map { it.trim().split(" ").filter { it.isNotEmpty() }.map { it.toInt() } }
        }.mapIndexed { currentCardIndex, currentCard ->
            val cardsWon = currentCard[0].intersect(currentCard[1]).size
            val amountOfCurrentCard = amountOfCard.getOrDefault(currentCardIndex, 1)
            ((currentCardIndex+1)..currentCardIndex+cardsWon).forEach {
                amountOfCard[it] = amountOfCard.getOrDefault(it, 1) + amountOfCurrentCard
            }
            amountOfCurrentCard
        }.sum()
    println(totalAmountOfCards)
}

fun main() {
    part1(File("inputs/04-part1.txt"))
    part1(File("inputs/04.txt"))
    println("---")
    part2(File("inputs/04-part1.txt"))
    part2(File("inputs/04.txt"))
}
