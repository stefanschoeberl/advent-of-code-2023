package dev.ssch.day07

import java.io.File

enum class HandType {
    HighCard,
    OnePair,
    TwoPair,
    ThreeOfKind,
    FullHouse,
    FourOfKind,
    FiveOfKind,
}

fun mapCardValuesWithStrengthOrder(cards: String, cardStrengthOrder: List<Char>): String {
    return cards.map {
        cardStrengthOrder.indexOf(it).toString().padStart(3, '0')
    }.joinToString("")
}

fun computeHandType(cards: String): HandType {
    val letterCounts = cards
        .groupBy { it }
        .values
        .map { it.size }
        .sortedDescending()

    val highestAmount = letterCounts[0]
    val secondHighestAmount = letterCounts.getOrNull(1)

    return if (highestAmount == 5) {
        HandType.FiveOfKind
    } else if (highestAmount == 4) {
        HandType.FourOfKind
    } else if (highestAmount == 3) {
        if (secondHighestAmount == 2) {
            HandType.FullHouse
        } else {
            HandType.ThreeOfKind
        }
    } else if (highestAmount == 2) {
        if (secondHighestAmount == 2) {
            HandType.TwoPair
        } else {
            HandType.OnePair
        }
    } else {
        HandType.HighCard
    }
}

fun part1(file: File) {
    val lines = file.readLines()

    val hands = lines.map {
        val (cards, bid) = it.split(" ")
        Pair(cards, bid.toInt())
    }

    val cardStrengthOrder = listOf('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2').reversed()

    val handsSorted = hands.sortedWith(Comparator
        .comparing<Pair<String, Int>?, HandType?> { computeHandType(it.first) }
        .thenComparing { cards -> mapCardValuesWithStrengthOrder(cards.first, cardStrengthOrder) })

    val totalWinnings = handsSorted.mapIndexed { index, hand ->
        hand.second * (index + 1)
    }.sum()
    println(totalWinnings)
}

// ---

fun computeHandTypeWithJokers(cards: String): HandType {
    val numberOfJokers = cards.count { it == 'J' }

    val letterCountWithoutJokers = cards
        .filter { it != 'J' }
        .groupBy { it }
        .values
        .map { it.size }
        .sortedDescending().let {
            it.ifEmpty {
                listOf(0)
            }
        }

    val highestAmount = letterCountWithoutJokers[0]
    val secondHighestAmount = letterCountWithoutJokers.getOrNull(1)

    return if (highestAmount + numberOfJokers == 5) {
        HandType.FiveOfKind
    } else if (highestAmount + numberOfJokers == 4) {
        HandType.FourOfKind
    } else if (highestAmount + numberOfJokers >= 3) {
        val remainingJokers = highestAmount + numberOfJokers - 3
        if (secondHighestAmount?.plus(remainingJokers) == 2) {
            HandType.FullHouse
        } else {
            HandType.ThreeOfKind
        }
    } else if (highestAmount + numberOfJokers >= 2) {
        val remainingJokers = highestAmount + numberOfJokers - 2
        if (secondHighestAmount?.plus(remainingJokers) == 2) {
            HandType.TwoPair
        } else {
            HandType.OnePair
        }
    } else {
        HandType.HighCard
    }
}

fun part2(file: File) {
    val lines = file.readLines()

    val hands = lines.map {
        val (cards, bid) = it.split(" ")
        Pair(cards, bid.toInt())
    }

    val cardStrengthOrder = listOf('A', 'K', 'Q', 'T', '9', '8', '7', '6', '5', '4', '3', '2', 'J').reversed()

    val handsSorted = hands.sortedWith(Comparator
        .comparing<Pair<String, Int>?, HandType?> { computeHandTypeWithJokers(it.first) }
        .thenComparing { cards -> mapCardValuesWithStrengthOrder(cards.first, cardStrengthOrder) })

    val totalWinnings = handsSorted.mapIndexed { index, hand ->
        hand.second * (index + 1)
    }.sum()
    println(totalWinnings)
}

fun main() {
    part1(File("inputs/07-part1.txt"))
    part1(File("inputs/07.txt"))
    println("---")
    part2(File("inputs/07-part1.txt"))
    part2(File("inputs/07.txt"))
}
