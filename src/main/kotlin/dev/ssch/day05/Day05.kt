package dev.ssch.day05

import java.io.File

fun <T> List<T>.split(pred: (T) -> Boolean): List<List<T>> {
    data class Acc(
        val result: List<List<T>>,
        val currentChunk: List<T>,
    )

    return foldIndexed(Acc(emptyList(), emptyList())) { index: Int, acc: Acc, value: T ->
        if (pred(value)) {
            // splitter element found, add current chunk to result, discard current element
            Acc(acc.result.plusElement(acc.currentChunk), emptyList())
        } else if (index == size - 1) {
            // last (non-splitter) element encountered, finish last chunk and add it to the result
            Acc(acc.result.plusElement(acc.currentChunk + value), emptyList())
        } else {
            // add current (non-splitter) element to current chunk
            Acc(acc.result, acc.currentChunk + value)
        }
    }.result
}

data class RangeMapping(
    val destination: Long,
    val source: Long,
    val length: Long
)

fun Long.canBeMappedBy(mapping: RangeMapping): Boolean {
    return this >= mapping.source && this < mapping.source + mapping.length
}

fun Long.transformWithMappings(mappings: List<RangeMapping>): Long {
    return mappings.find { this.canBeMappedBy(it) }?.let {
        (this - it.source) + it.destination
    } ?: this
}

private fun parseSteps(blocks: List<List<String>>): List<List<RangeMapping>> {
    return blocks
        .map {
            val mappings = it
                .drop(1)
                .map {
                    val (destinationNumber, sourceNumber, range) = it.split(" ").map { it.toLong() }
                    RangeMapping(destinationNumber, sourceNumber, range)
                }
            mappings
        }
}

fun part1(file: File) {
    val lines = file.readLines()

    val allBlocks = lines.split { it.isEmpty() }

    val initialSeeds = allBlocks
        .first().first()
        .split(": ")[1]
        .split(" ")
        .map { it.toLong() }

    val steps = parseSteps(allBlocks.drop(1))

    val finalLocations = steps
        .fold(initialSeeds) { seeds, mappings ->
            seeds.map {
                it.transformWithMappings(mappings)
            }
        }

    println(finalLocations.min())
}

// ---

data class SeedRange(
    val start: Long,
    val length: Long,
) {
    private fun splitRange(startsOfCuts: List<Long>): List<SeedRange> {
        return (startsOfCuts + listOf(start, start + length))
            .filter { it >= start && it <= start + length }
            .sorted()
            .distinct()
            .windowed(2)
            .map { SeedRange(it[0], it[1] - it[0]) }
            .toList()
    }

    fun transformWithMappings(mappings: List<RangeMapping>): List<SeedRange> {
        val startOfCuts = mappings.flatMap {
            listOf(it.source, it.source + it.length)
        }

        return splitRange(startOfCuts)
            .map { subRange ->
                val newStart = subRange.start.transformWithMappings(mappings)
                if (start != newStart) {
                    SeedRange(newStart, subRange.length)
                } else {
                    subRange
                }
            }
    }
}

fun part2(file: File) {
    val lines = file.readLines()

    val allBlocks = lines.split { it.isEmpty() }

    val initialSeedRanges = allBlocks
        .first().first()
        .split(": ")[1]
        .split(" ")
        .map { it.toLong() }
        .chunked(2)
        .map { SeedRange(it[0], it[1]) }

    val steps = parseSteps(allBlocks.drop(1))

    val finalLocations = steps
        .fold(initialSeedRanges) { seeds, mappings ->
            seeds.flatMap {
                it.transformWithMappings(mappings)
            }
        }

    println(finalLocations.minOf { it.start })
}

fun main() {
    part1(File("inputs/05-part1.txt"))
    part1(File("inputs/05.txt"))
    println("---")
    part2(File("inputs/05-part1.txt"))
    part2(File("inputs/05.txt"))
}
