package dev.ssch.day12

import java.io.File

fun solutionToString(segmentSizes: List<Int>, segmentStarts: List<Int>, length: Int): String {
    return segmentStarts.runningFoldIndexed("") { index, acc, segmentStartIndex ->
        val segment = "#".repeat(segmentSizes[index])
        acc.padEnd(segmentStartIndex, '.') + segment
    }.last().padEnd(length, '.')
}

fun isPossibleSolution(conditionRecord: String, segmentSizes: List<Int>, segmentStarts: List<Int>): Boolean {
    val solutionAsString = solutionToString(segmentSizes, segmentStarts, conditionRecord.length)
    return segmentSizes.size == segmentStarts.size && conditionRecord.zip(solutionAsString)
        .all { it.first == '?' || it.first == it.second }
}

fun isPartialSolution(conditionRecord: String, segmentSizes: List<Int>, segmentStarts: List<Int>): Boolean {
    return conditionRecord.zip(solutionToString(segmentSizes, segmentStarts, conditionRecord.length))
        .take(segmentStarts.last() + segmentSizes[segmentStarts.lastIndex])
        .all {
            it.first == '?' || it.first == it.second
        }
}

fun countPossibilities(conditionRecord: String, segments: List<Int>, segmentsStarts: List<Int> = emptyList()): Long {
    if (isPossibleSolution(conditionRecord, segments, segmentsStarts)) {
        return 1
    } else if (segmentsStarts.size == segments.size) {
        return 0
    }

    val currentSegment = segmentsStarts.size
    val currentSegmentSize = segments[currentSegment]
    val firstStartIndexForCurrentSegment = segmentsStarts.lastOrNull()?.let {
        it + segments[segmentsStarts.lastIndex] + 1
    } ?: 0

    val lastStartIndexForCurrentSegment =
        conditionRecord.length - segments.drop(currentSegment + 1).sumOf { it + 1 } - currentSegmentSize

    return (firstStartIndexForCurrentSegment..lastStartIndexForCurrentSegment)
        .sumOf {
            val newSegmentStarts = segmentsStarts + it
            if (isPartialSolution(conditionRecord, segments, newSegmentStarts)) {
                countPossibilities(conditionRecord, segments, newSegmentStarts)
            } else {
                0
            }
        }
}

fun part1(file: File) {
    val lines = file.readLines()

    val totalPossibilities = lines.map { line ->
        val (conditionRecord, segmentSizes) = line.split(" ")
        Pair(conditionRecord, segmentSizes.split(",").map { it.toInt() })
    }.sumOf { (springs, numbers) ->
        countPossibilities(springs, numbers)
    }

    println(totalPossibilities)
}

// ---

typealias CacheKey = Pair<String, List<Int>>

// (conditionRecord, segmentSizes) => number of solutions
val cache = mutableMapOf<CacheKey, Long>()
fun useCacheOrCompute(cacheKey: CacheKey, computeFunction: () -> Long): Long {
    return cache[cacheKey] ?: run {
        computeFunction().also {
            cache[cacheKey] = it
        }
    }
}

fun countPossibilitiesWithCache(conditionRecord: String, segmentSizes: List<Int>): Long {
    return useCacheOrCompute(Pair(conditionRecord, segmentSizes)) {
        if (segmentSizes.isEmpty()) {
            if (conditionRecord.contains("#")) {
                0
            } else {
                1
            }
        } else {
            val currentSegmentSize = segmentSizes.first()
            val remainingSegmentSizes = segmentSizes.drop(1)
            val lastStartIndexForCurrentSegment =
                conditionRecord.length - remainingSegmentSizes.sumOf { it + 1 } - currentSegmentSize
            (0..lastStartIndexForCurrentSegment).filter { startIndex ->
                // check valid segment range
                // 1. condition record does not have '.' (only '#' and '?') at the current range
                // 2. condition record has no '#'s before the current range (prevent missing '#'s)
                // 3. condition record has no '#' directly after the current range (spacer to next segment)
                conditionRecord.substring(startIndex, startIndex + currentSegmentSize).all { it != '.' }
                        && conditionRecord.take(startIndex).all { it != '#' }
                        && conditionRecord.getOrNull(startIndex + currentSegmentSize)?.let { it != '#' } ?: true
            }.sumOf { startIndex ->
                val remainingConditionRecord =
                    conditionRecord.drop(startIndex + currentSegmentSize + 1) // +1 for spacer
                countPossibilitiesWithCache(remainingConditionRecord, remainingSegmentSizes)
            }
        }
    }
}

fun part2(file: File) {
    val lines = file.readLines()

    val totalPossibilities = lines.map { line ->
        val (conditionRecord, segmentSizes) = line.split(" ")
        Pair(conditionRecord, segmentSizes.split(",").map { it.toInt() })
    }.sumOf { (springs, numbers) ->
        val conditionRecordsExpanded = (1..5).joinToString("?") { springs }
        val segmentSizesExpanded = (1..5).flatMap { numbers }
        countPossibilitiesWithCache(conditionRecordsExpanded, segmentSizesExpanded)
    }

    println(totalPossibilities)
}

fun main() {
    part1(File("inputs/12-part1.txt"))
    part1(File("inputs/12.txt"))
    println("---")
    part2(File("inputs/12-part1.txt"))
    part2(File("inputs/12.txt"))
}
