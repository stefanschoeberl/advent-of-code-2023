package dev.ssch.day17

import java.io.File
import java.util.*
import kotlin.Comparator

enum class Direction {
    Up, Right, Down, Left
}

data class Position(
    val row: Int,
    val col: Int,
) {
    fun moveBy(deltaRow: Int, deltaCol: Int): Position {
        return Position(row + deltaRow, col + deltaCol)
    }
}

enum class TurnDirection {
    Left, Right
}

data class State(
    val position: Position,
    val direction: Direction,
) {
    fun moveInCurrentDirectionBy(amount: Int): State {
        return when (direction) {
            Direction.Up -> State(this.position.moveBy(-amount, 0), this.direction)
            Direction.Right -> State(this.position.moveBy(0, amount), this.direction)
            Direction.Down -> State(this.position.moveBy(amount, 0), this.direction)
            Direction.Left -> State(this.position.moveBy(0, -amount), this.direction)
        }
    }

    fun turn(turnDirection: TurnDirection): State {
        return when (turnDirection) {
            TurnDirection.Left -> State(
                this.position, when (direction) {
                    Direction.Up -> Direction.Left
                    Direction.Right -> Direction.Up
                    Direction.Down -> Direction.Right
                    Direction.Left -> Direction.Down
                }
            )
            TurnDirection.Right -> State(
                this.position, when (direction) {
                    Direction.Up -> Direction.Right
                    Direction.Right -> Direction.Down
                    Direction.Down -> Direction.Left
                    Direction.Left -> Direction.Up
                }
            )
        }
    }
}

fun List<String>.getValue(position: Position): Int {
    return this[position.row][position.col].digitToInt()
}

data class CrucibleConstraints(
    val minimumConsecutiveBlocks: Int,
    val maximumConsecutiveBlocks: Int,
)

fun State.reachableStates(grid: List<String>, crucibleConstraints: CrucibleConstraints): Sequence<Pair<State, Int>> {
    return (1..crucibleConstraints.maximumConsecutiveBlocks).asSequence()
        .map { this.moveInCurrentDirectionBy(it) }
        .filter { it.isWithinGrid(grid) }
        .map { Pair(it, grid.getValue(it.position)) }
        .runningReduce { acc, current ->
            Pair(current.first, acc.second + current.second)
        }
        .drop(crucibleConstraints.minimumConsecutiveBlocks - 1)
        .flatMap {
            TurnDirection.entries.map { turnDirection ->
                Pair(it.first.turn(turnDirection), it.second)
            }
        }
}

private fun State.isWithinGrid(grid: List<String>): Boolean {
    return grid.indices.contains(this.position.row) && grid.first().indices.contains(this.position.col)
}

fun findMinimalHeatLoss(grid: List<String>, crucibleConstraints: CrucibleConstraints): Int? {
    val startStateRight = State(Position(0, 0), Direction.Right)
    val startStateDown = State(Position(0, 0), Direction.Down)

    val goalPosition = Position(grid.lastIndex, grid.first().lastIndex)

    val previousState = mutableMapOf<State, State>()
    val distanceFromStart = mutableMapOf<State, Int>().withDefault { Integer.MAX_VALUE }
    distanceFromStart[startStateRight] = 0
    distanceFromStart[startStateDown] = 0

    val visitedStates = mutableSetOf<State>()
    val queue = PriorityQueue<State>(Comparator.comparing { distanceFromStart.getValue(it) })
    queue.offer(startStateRight)
    queue.offer(startStateDown)

    var goalState: State? = null
    while (queue.isNotEmpty() && goalState == null) {
        val currentState = queue.peek()
        queue.remove(currentState)

        if (!visitedStates.contains(currentState)) {
            visitedStates.add(currentState)

            if (currentState.position == goalPosition) {
                goalState = currentState
            } else {
                currentState
                    .reachableStates(grid, crucibleConstraints)
                    .forEach { (neighbor, heatLoss) ->
                        val totalDistanceToNeighbor = distanceFromStart.getValue(currentState) + heatLoss
                        if (totalDistanceToNeighbor < distanceFromStart.getValue(neighbor)) {
                            distanceFromStart[neighbor] = totalDistanceToNeighbor
                            previousState[neighbor] = currentState
                            queue.offer(neighbor)
                        }
                    }
            }
        }
    }

    fun printRoute() {
        val visitedPositions = generateSequence(goalState) { previousState[it] }
            .map { it.position to it.direction }
            .toMap()

        grid.indices.forEach { row ->
            println(grid.first().indices.map { col ->
                visitedPositions[Position(row, col)]?.let {
                    when (it) {
                        Direction.Up -> '^'
                        Direction.Right -> '>'
                        Direction.Down -> 'V'
                        Direction.Left -> '<'
                    }
                } ?: '.'
            }.joinToString(""))
        }
    }

//    printRoute()
    return goalState?.let { distanceFromStart[it] }
}

fun part1(file: File) {
    val grid = file.readLines()
    val minimumHeatLoss = findMinimalHeatLoss(grid, CrucibleConstraints(1, 3))
    println(minimumHeatLoss)
}

// ---

fun part2(file: File) {
    val grid = file.readLines()
    val minimumHeatLoss = findMinimalHeatLoss(grid, CrucibleConstraints(4, 10))
    println(minimumHeatLoss)
}

fun main() {
    part1(File("inputs/17-part1.txt"))
    part1(File("inputs/17.txt"))
    println("---")
    part2(File("inputs/17-part1.txt"))
    part2(File("inputs/17-part2.txt"))
    part2(File("inputs/17.txt"))
}
