package dev.ssch.day25

import java.io.File
import kotlin.random.Random
import kotlin.random.nextInt

data class Graph(
    val nodes: List<String>,
    val edges: Map<String, List<String>>
)

fun buildGraph(lines: List<String>): Graph {
    val edges = lines.flatMap { line ->
        val (node, otherNodesText) = line.split(":").map { it.trim() }
        val otherNodes = otherNodesText.split(" ").map { it.trim() }
        otherNodes.flatMap { listOf(node to it, it to node) }
    }.groupBy({ it.first }, { it.second })
    return Graph(edges.keys.toList(), edges)
}

data class Edge(
    val nodes: Set<String>,
) {
    constructor(from: String, to: String) : this(setOf(from, to))
}

fun traverseGraphRandomly(graph: Graph, random: Random): Set<Edge> {
    val from = graph.nodes[random.nextInt(graph.nodes.indices)]
    val to = graph.nodes[random.nextInt(graph.nodes.indices)]

    data class Acc(
        val nodesToExpand: List<String>,
        val visitedNodes: Set<String>,
        val previousNode: Map<String, String>,
    )

    val previousNode =
        generateSequence(Acc(listOf(from), emptySet(), emptyMap())) { (nodesToExpand, visitedNodes, previousNode) ->
            val currentNode = nodesToExpand.first()
            val newPrevious = graph.edges[currentNode]!!
                .filter { !visitedNodes.contains(it) && !previousNode.containsKey(it) }
                .map { it to currentNode }

            Acc(
                nodesToExpand.drop(1) + newPrevious.map { it.first },
                visitedNodes + currentNode,
                previousNode + newPrevious
            )
        }.first { it.visitedNodes.contains(to) }.previousNode

    val path = generateSequence(listOf(to)) { path ->
        path + previousNode[path.last()]!!
    }.first { it.last() == from }

    return path.windowed(2)
        .map { (from, to) -> Edge(from, to) }
        .toSet()
}

fun findEdgesToCut(graph: Graph): Set<Edge> {
    val random = Random(1234)
    val edgeVisitCount = (1..1000)
        .flatMap { traverseGraphRandomly(graph, random) }
        .groupBy { it }.mapValues { it.value.size }
    return edgeVisitCount.entries
        .sortedByDescending { it.value }
        .take(3)
        .map { it.key }
        .toSet()
}

fun computeGroupSizes(graph: Graph, edgesToCut: Set<Edge>): Pair<Int, Int> {
    data class Acc(
        val nodesToExpand: Set<String>,
        val visitedNodes: Set<String>,
    )

    val numberOfVisitedNodes =
        generateSequence(Acc(setOf(edgesToCut.first().nodes.first()), emptySet())) { (nodesToExpand, visitedNodes) ->
            val currentNode = nodesToExpand.first()
            if (visitedNodes.contains(currentNode)) {
                Acc(
                    nodesToExpand - currentNode,
                    visitedNodes
                )
            } else {
                val newNodes = graph.edges[currentNode]!!
                    .filter { !edgesToCut.contains(Edge(currentNode, it)) }
                    .filter { !visitedNodes.contains(it) }
                Acc(
                    nodesToExpand - currentNode + newNodes,
                    visitedNodes + currentNode
                )
            }
        }.first { it.nodesToExpand.isEmpty() }.visitedNodes.size

    return Pair(numberOfVisitedNodes, graph.nodes.size - numberOfVisitedNodes)
}

fun part1(file: File) {
    val lines = file.readLines()
    val graph = buildGraph(lines)
    val edgesToCut = findEdgesToCut(graph)
    val (groupA, groupB) = computeGroupSizes(graph, edgesToCut)
    println(groupA * groupB)
}

fun main() {
    part1(File("inputs/25-part1.txt"))
    part1(File("inputs/25.txt"))
    println("---")
}
