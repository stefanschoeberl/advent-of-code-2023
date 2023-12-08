package dev.ssch.day08

import java.io.File
import java.lang.RuntimeException
import java.math.BigInteger

fun part1(file: File) {
    val lines = file.readLines()

    val instructions = lines.first()
    val crossings = lines.drop(2).associate {
        it.substring(0..2) to (it.substring(7..9) to it.substring(12..14))
    }

    val instructionsLooped = generateSequence(0) {
        (it + 1) % instructions.length
    }.map {
        instructions[it]
    }

    val numberOfSteps = instructionsLooped.runningFold("AAA") { currentNode, direction ->
        if (direction == 'L') {
            crossings[currentNode]!!.first
        } else {
            crossings[currentNode]!!.second
        }
    }.takeWhile { it != "ZZZ" }.count()

    println(numberOfSteps)
}

// ---

fun factorInteger(n: Int): List<Pair<Int, Int>> {
    data class Acc(
        val remainingNumber: Int,
        val factors: List<Pair<Int, Int>>
    )

    return (2..n)
        .fold(Acc(
            n,
            emptyList()
        )) { acc, currentDivisor ->
            val divisionSteps = generateSequence(acc.remainingNumber) {
                if (it % currentDivisor == 0) {
                    it / currentDivisor
                } else {
                    null
                }
            }.drop(1).toList()

            if (divisionSteps.isNotEmpty()) {
                Acc(
                    divisionSteps.last(),
                    acc.factors + Pair(currentDivisor, divisionSteps.size)
                )
            } else {
                acc
            }
        }.factors
}

fun pow(base: Int, exp: Int): Long {
    return BigInteger.valueOf(base.toLong()).pow(exp).toLong()
}

fun computeLowestCommonMultiple(numbers: List<Int>): Long {
    return numbers.flatMap {
        factorInteger(it)
    }.groupBy({ it.first }, { it.second })
        .mapValues { it.value.max() }
        .asSequence()
        .fold(1L) { acc, entry ->
            acc * pow(entry.key, entry.value)
        }
}

fun part2(file: File) {
    val lines = file.readLines()

    val instructions = lines.first()
    val crossings = lines.drop(2).associate {
        it.substring(0..2) to (it.substring(7..9) to it.substring(12..14))
    }

    val instructionsLooped = generateSequence(0) {
        (it + 1) % instructions.length
    }.map {
        instructions[it]
    }

    val startingNodes = crossings.keys.filter { it.endsWith("A") }

    // Try assumption: Each ghost has an equal loop length (forever), test for the first 100 iterations
    val loopLengths = startingNodes.map {
        instructionsLooped.runningFold(Pair(it, 1)) { currentNode, direction ->
            if (direction == 'L') {
                Pair(crossings[currentNode.first]!!.first, currentNode.second + 1)
            } else {
                Pair(crossings[currentNode.first]!!.second, currentNode.second + 1)
            }
        }.filter { it.first.endsWith("Z") }.take(100).map { it.second }.toList()
    }.map {
        val loopLengths = it.windowed(2)
            .map { (a, b) -> b - a }
            .distinct()
        if (loopLengths.size == 1) {
            loopLengths.first()
        } else {
            throw RuntimeException("one ghost has different loop lengths")
        }
    }

    println(computeLowestCommonMultiple(loopLengths))
}

fun main() {
    part1(File("inputs/08-part1.txt"))
    part1(File("inputs/08.txt"))
    println("---")
    part2(File("inputs/08-part2.txt"))
    part2(File("inputs/08.txt"))
}
