package dev.ssch.day15

import java.io.File
import java.lang.RuntimeException

fun computeHash(text: String): Int {
    return text.fold(0) { acc, c ->
        ((acc + c.code) * 17) % 256
    }
}

fun part1(file: File) {
    val line = file.readLines()[0]

    val sum = line.split(',').sumOf { computeHash(it) }
    println(sum)
}

// ---

fun part2(file: File) {
    val line = file.readLines()[0]

    val boxes = line.split(',').fold(emptyMap<Int, List<Pair<String, Int>>>()) { acc, instruction ->
        val (label, command, number) = Regex("([^=-]+)([=-])([0-9]*)").find(instruction)!!.destructured

        val hash = computeHash(label)

        val boxContent = acc[hash] ?: emptyList()

        when (command) {
            "=" -> if (boxContent.any { it.first == label }) {
                val newBoxContent = boxContent.map {
                    if (it.first == label) {
                        Pair(it.first, number.toInt())
                    } else {
                        it
                    }
                }
                acc + (hash to newBoxContent)
            } else {
                acc + (hash to (boxContent + Pair(label, number.toInt())))
            }
            "-" -> acc + (hash to (acc[hash]?.filter { it.first != label } ?: emptyList()))
            else -> throw RuntimeException("unreachable")
        }
    }

    val sum = boxes.flatMap { (boxNumber, content) ->
        content.mapIndexed { index, lens ->
            (boxNumber + 1) * (index + 1) * lens.second
        }
    }.sum()

    println(sum)
}

fun main() {
    part1(File("inputs/15-part1.txt"))
    part1(File("inputs/15.txt"))
    println("---")
    part2(File("inputs/15-part1.txt"))
    part2(File("inputs/15.txt"))
}
