package dev.ssch.day23

import java.io.File
import java.lang.RuntimeException

data class Position(
    val row: Int,
    val col: Int,
) {

    fun get4Neighborhood(): List<Position> {
        return listOf(
            Position(row + 1, col),
            Position(row - 1, col),
            Position(row, col + 1),
            Position(row, col - 1),
        )
    }

    fun up(): Position {
        return Position(row - 1, col)
    }

    fun right(): Position {
        return Position(row, col + 1)
    }

    fun down(): Position {
        return Position(row + 1, col)
    }

    fun left(): Position {
        return Position(row, col - 1)
    }

}

object Tile {
    const val path = '.'
    const val wall = '#'
    const val slopeUp = '^'
    const val slopeRight = '>'
    const val slopeDown = 'v'
    const val slopeLeft = '<'
}

typealias Grid = List<String>

fun Grid.positions(): Sequence<Position> {
    return this.asSequence().flatMapIndexed { row, line ->
        line.indices.map { Position(row, it) }
    }
}

fun Grid.getTile(position: Position): Char {
    return this[position.row][position.col]
}

fun Grid.isWithinBounds(position: Position): Boolean {
    return (position.row in this.indices) && (position.col in this.first().indices)
}

data class Graph(
    val nodes: Set<Position>,
    val edges: Map<Position, Map<Position, Int>>,
    val startPosition: Position,
    val endPosition: Position,
)

fun findReachableNodes(grid: Grid, allNodes: Set<Position>, start: Position, ignoreSlopes: Boolean): Map<Position, Int> {
    data class Acc(
        val visitedNodes: Set<Position>,
        val openNodes: List<Pair<Position, Int>>,
        val reachableNodes: Map<Position, Int>,
    )

    return generateSequence(
        Acc(
            emptySet(),
            listOf(Pair(start, 0)),
            emptyMap()
        )
    ) { (visitedNodes, openNodes, reachableNodes) ->
        val (currentNode, currentDistance) = openNodes.first()

        fun Position.expandWithSlopes(): Sequence<Position> {
            return when (grid.getTile(this)) {
                Tile.path -> this.get4Neighborhood().asSequence()
                Tile.slopeUp -> sequenceOf(this.up())
                Tile.slopeRight -> sequenceOf(this.right())
                Tile.slopeDown -> sequenceOf(this.down())
                Tile.slopeLeft -> sequenceOf(this.left())
                else -> throw RuntimeException("unreachable")
            }
        }

        val neighbors = currentNode
            .let { if (ignoreSlopes) it.get4Neighborhood().asSequence() else it.expandWithSlopes() }
            .filter { !visitedNodes.contains(it) && grid.isWithinBounds(it) && grid.getTile(it) != Tile.wall }
            .map { it to currentDistance + 1 }

        val (nodeNeighbors, pathNeighbors) = neighbors.partition { allNodes.contains(it.first) }

        Acc(
            visitedNodes + currentNode,
            openNodes.drop(1) + pathNeighbors,
            reachableNodes + nodeNeighbors
        )
    }.first { it.openNodes.isEmpty() }.reachableNodes
}

fun buildGraph(grid: Grid, ignoreSlopes: Boolean): Graph {
    val startPosition = Position(grid.indices.first, grid.first().indexOf(Tile.path))
    val endPosition = Position(grid.indices.last, grid.last().indexOf(Tile.path))
    val crossings = grid.positions()
        .filter { currentPosition ->
            grid.getTile(currentPosition) != Tile.wall &&
                    currentPosition.get4Neighborhood()
                        .filter { grid.isWithinBounds(it) }
                        .map { grid.getTile(it) }
                        .count { it != Tile.wall } >= 3
        }.toSet()

    val nodes = crossings + startPosition + endPosition
    val edges = nodes.associateWith { findReachableNodes(grid, nodes, it, ignoreSlopes) }

    return Graph(
        nodes,
        edges,
        startPosition,
        endPosition
    )
}

private fun generateGraphVizGraph(graph: Graph, fileName: String) {
    fun Position.graphVizName(): String {
        return "\"${this.row}/${this.col}\""
    }
    val nodes = graph.nodes.joinToString("\n") { it.graphVizName() }
    val edges = graph.edges
        .flatMap { (from, edges) -> edges.map { (to, distance) -> Triple(from, to, distance) } }
        .joinToString("\n") { (from, to, distance) -> "${from.graphVizName()} -> ${to.graphVizName()} [label=$distance]" }
    File(fileName).writeText("digraph{\n$nodes\n$edges\n}")
}

fun findLongestPath(graph: Graph): Int {
    // visualization revealed acyclic structure => length of longest path starting at each node can be cached easily
    val cache = mutableMapOf<Position, Int>()
    fun useCacheOrCompute(cacheKey: Position, computeFunction: () -> Int): Int {
        return cache[cacheKey] ?: run {
            computeFunction().also {
                cache[cacheKey] = it
            }
        }
    }

    fun findLongestPathFrom(node: Position): Int {
        return useCacheOrCompute(node) {
            if (node == graph.endPosition) {
                0
            } else {
                graph.edges[node]!!.maxOf { (to, distance) -> findLongestPathFrom(to) + distance }
            }
        }
    }

    return findLongestPathFrom(graph.startPosition)
}

fun part1(file: File) {
    val grid = file.readLines()
    val graph = buildGraph(grid, false)
    generateGraphVizGraph(graph, "outputs/23-graph-${file.name}.txt")
    println(findLongestPath(graph))
}

// ---

fun findLongestPathBruteforce(graph: Graph): Int {
    // simple caching is no longer possible because of cycles => try bruteforce
    fun findLongestPathFrom(currentNode: Position, visitedNodes: Set<Position> = emptySet(), pathLength: Int = 0): Int? {
        if (currentNode == graph.endPosition) {
            return pathLength
        } else {
            val updatedVisitedNodes = visitedNodes + currentNode
            return graph.edges[currentNode]!!
                .filter { (to, _) -> !visitedNodes.contains(to) }
                .mapNotNull { (to, distance) ->
                    findLongestPathFrom(to, updatedVisitedNodes, pathLength + distance)
                }
                .maxOrNull()
        }
    }
    return findLongestPathFrom(graph.startPosition)!!
}

fun part2(file: File) {
    val grid = file.readLines()
    val graph = buildGraph(grid, true)
    generateGraphVizGraph(graph, "outputs/23-part2-graph-${file.name}.txt")
    println(findLongestPathBruteforce(graph))
}

fun main() {
    part1(File("inputs/23-part1.txt"))
    part1(File("inputs/23.txt"))
    println("---")
    part2(File("inputs/23-part1.txt"))
    part2(File("inputs/23.txt"))
}
