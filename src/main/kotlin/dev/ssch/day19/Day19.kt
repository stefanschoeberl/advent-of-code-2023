package dev.ssch.day19

import dev.ssch.day05.split
import java.io.File
import java.lang.RuntimeException

data class Part(
    val rating: Map<Char, Int>
)

enum class Comparison {
    LowerThan, GreaterThan
}

data class Rule(
    val category: Char,
    val comparison: Comparison,
    val threshold: Int,
    val nextWorkflow: String
)

data class Workflow(
    val name: String,
    val rules: List<Rule>,
    val fallbackNextWorkflow: String
)

fun parseRule(str: String): Rule {
    val (category, comparisonString, thresholdString, nextWorkflow) =
        Regex("""([xmas])([><])([0-9]+):([AR]|[a-z]+)""").matchEntire(str)!!.destructured

    val comparison = when (comparisonString) {
        ">" -> Comparison.GreaterThan
        "<" -> Comparison.LowerThan
        else -> throw RuntimeException("unreachable")
    }

    return Rule(
        category.first(),
        comparison,
        thresholdString.toInt(),
        nextWorkflow
    )
}

fun parseWorkflow(str: String): Workflow {
    val (name, rulesString) = Regex("""([a-z]+)\{([^}]+)}""").matchEntire(str)!!.destructured
    val rules = rulesString.split(",")
    return Workflow(
        name,
        rules.dropLast(1).map { parseRule(it) },
        rules.last()
    )
}

fun parsePart(str: String): Part {
    val ratings = str
        .drop(1)
        .dropLast(1)
        .split(",").associate {
            val (category, rating) = it.split("=")
            category.first() to rating.toInt()
        }
    return Part(ratings)
}

fun part1(file: File) {
    val lines = file.readLines()

    val (workflows, parts) = lines
        .split { it.isEmpty() }
        .let { (workflows, parts) ->
            Pair(
                workflows.map { parseWorkflow(it) },
                parts.map { parsePart(it) },
            )
        }

    val acceptedParts = parts.filter {
        isPartAccepted(workflows, it)
    }

    val sum = acceptedParts.sumOf { it.rating.values.sum() }
    println(sum)
}

fun isPartAccepted(workflows: List<Workflow>, part: Part): Boolean {
    val result = generateSequence("in") { workflowName ->
        val workflow = workflows.first { it.name == workflowName }
        val nextWorkflow = workflow.rules.firstOrNull { rule ->
            when (rule.comparison) {
                Comparison.LowerThan -> part.rating[rule.category]!! < rule.threshold
                Comparison.GreaterThan -> part.rating[rule.category]!! > rule.threshold
            }
        }?.nextWorkflow ?: workflow.fallbackNextWorkflow
        nextWorkflow
    }.first {
        it == "A" || it == "R"
    }
    return result == "A"
}

// ---

data class PartWithRanges(
    val rating: Map<Char, IntRange>,
)

fun processPart(part: PartWithRanges, workflow: Workflow): List<Pair<PartWithRanges, String>> {

    fun wholeRangeMatchedByRule(part: PartWithRanges, rule: Rule): Boolean {
        return (rule.comparison == Comparison.LowerThan && part.rating[rule.category]!!.last < rule.threshold)
                || (rule.comparison == Comparison.GreaterThan && part.rating[rule.category]!!.first > rule.threshold)
    }

    fun wholeRangeNotMatchedByRule(part: PartWithRanges, rule: Rule): Boolean {
        return (rule.comparison == Comparison.LowerThan && part.rating[rule.category]!!.last <= rule.threshold)
                || (rule.comparison == Comparison.GreaterThan && part.rating[rule.category]!!.first >= rule.threshold)
    }

    fun splitPartRange(currentPart: PartWithRanges, rule: Rule): Pair<PartWithRanges, PartWithRanges> {
        val range = currentPart.rating[rule.category]!!
        val (matchingRange, notMatchingRange) = when (rule.comparison) {
            Comparison.LowerThan -> Pair(range.first..<rule.threshold, rule.threshold..range.last)
            Comparison.GreaterThan -> Pair((rule.threshold + 1)..range.last, range.first..rule.threshold)
        }
        val notMatchingPart = PartWithRanges(currentPart.rating + (rule.category to notMatchingRange))
        val matchingPart = PartWithRanges(currentPart.rating + (rule.category to matchingRange))
        return Pair(notMatchingPart, matchingPart)
    }

    data class Acc(
        val partForNextStep: PartWithRanges?,
        val partsWithNextWorkflows: List<Pair<PartWithRanges, String>>,
        val finishedProcessing: Boolean
    )

    val (unprocessedPart, partsWithNextWorkflows) = workflow.rules.asSequence()
        .runningFold(Acc(part, emptyList(), false)) { acc, rule ->
            val (currentPart, partsWithNextWorkflows) = acc
            if (currentPart != null) {
                when {
                    wholeRangeMatchedByRule(currentPart, rule) ->
                        // add part to results, no part for next rule to process
                        Acc(
                            null,
                            partsWithNextWorkflows + Pair(currentPart, rule.nextWorkflow),
                            false
                        )

                    wholeRangeNotMatchedByRule(currentPart, rule) ->
                        // pass part to next rule
                        acc

                    else ->
                        // split part range
                        splitPartRange(currentPart, rule).let { (notMatchingPart, matchingPart) ->
                            Acc(
                                notMatchingPart,
                                partsWithNextWorkflows + Pair(matchingPart, rule.nextWorkflow),
                                false
                            )
                        }
                }
            } else {
                // no part to process, end rule processing
                Acc(
                    null,
                    partsWithNextWorkflows,
                    true
                )
            }
        }.takeWhile { !it.finishedProcessing }.last()

    return unprocessedPart?.let {
        partsWithNextWorkflows + Pair(it, workflow.fallbackNextWorkflow)
    } ?: partsWithNextWorkflows
}

fun part2(file: File) {
    val lines = file.readLines()

    val workflows = lines
        .split { it.isEmpty() }.first()
        .map { parseWorkflow(it) }
        .associateBy { it.name }

    val initialPart = PartWithRanges(
        mapOf(
            'x' to 1..4000,
            'm' to 1..4000,
            'a' to 1..4000,
            's' to 1..4000,
        )
    )

    data class Acc(
        val partsToProcess: List<Pair<PartWithRanges, String>>,
        val acceptedParts: Set<PartWithRanges>,
    )

    val acceptedParts = generateSequence(Acc(listOf(Pair(initialPart, "in")), emptySet())) { acc ->
        if (acc.partsToProcess.isNotEmpty()) {
            val (acceptedParts, remainingParts) = acc.partsToProcess
                .filter { it.second != "R" }
                .partition { it.second == "A" }
                .toList()
            val partsToProcess = remainingParts
                .flatMap { (part, workflowName) ->
                    val workflow = workflows[workflowName]!!
                    processPart(part, workflow)
                }
            Acc(partsToProcess, acc.acceptedParts + acceptedParts.map { it.first })
        } else {
            null
        }
    }.last().acceptedParts

    val combinations = acceptedParts.sumOf { part ->
        part.rating.values
            .map { it.last - it.first + 1 }
            .fold(1L) { acc, l -> acc * l.toLong() }
    }

    println(combinations)
}

fun main() {
    part1(File("inputs/19-part1.txt"))
    part1(File("inputs/19.txt"))
    println("---")
    part2(File("inputs/19-part1.txt"))
    part2(File("inputs/19.txt"))
}
