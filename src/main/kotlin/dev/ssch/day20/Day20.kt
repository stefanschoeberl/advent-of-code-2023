package dev.ssch.day20

import dev.ssch.day08.computeLowestCommonMultiple
import java.io.File
import kotlin.RuntimeException

fun isFlipFlop(definition: String) = definition.startsWith("%")
fun isConjunction(definition: String) = definition.startsWith("&")

const val broadcasterName = "broadcaster"

fun isBroadcaster(definition: String) = definition == broadcasterName

const val buttonName = "button"

fun getModuleName(definition: String): String {
    return when {
        isFlipFlop(definition) || isConjunction(definition) -> definition.drop(1)
        isBroadcaster(definition) -> definition
        else -> throw RuntimeException("unreachable")
    }
}

enum class Pulse {
    Low, High
}

sealed interface Module {
    val name: String
    val outgoingConnections: List<String>

    data class Broadcaster(
        override val name: String,
        override val outgoingConnections: List<String>
    ) : Module

    data class FlipFlop(
        override val name: String,
        override val outgoingConnections: List<String>,
        val isOn: Boolean
    ) : Module {
        fun flip(): FlipFlop {
            return FlipFlop(name, outgoingConnections, !isOn)
        }
    }

    data class Conjunction(
        override val name: String,
        override val outgoingConnections: List<String>,
        val memory: Map<String, Pulse>
    ) : Module {

        fun updateMemory(input: String, pulse: Pulse): Conjunction {
            return Conjunction(name, outgoingConnections, memory + (input to pulse))
        }
    }
}

fun parseModules(lines: List<String>): Map<String, Module> {
    val modulesPartlyParsed = lines.map { line ->
        val (name, connections) = line.split(" -> ")
        Pair(name, connections.split(",").map { it.trim() })
    }

    val incomingConnections = modulesPartlyParsed
        .flatMap { (from, outgoingConnections) ->
            outgoingConnections.map { to -> Pair(getModuleName(from), to) }
        }.groupBy({ it.second }, { it.first })

    return modulesPartlyParsed.map { (definition, outgoingConnections) ->
        when {
            isBroadcaster(definition) -> Module.Broadcaster(getModuleName(definition), outgoingConnections)
            isFlipFlop(definition) -> Module.FlipFlop(getModuleName(definition), outgoingConnections, false)
            isConjunction(definition) -> {
                val name = getModuleName(definition)
                val memory = incomingConnections[name]!!.associateWith { Pulse.Low }
                Module.Conjunction(name, outgoingConnections, memory)
            }

            else -> throw RuntimeException("unreachable")
        }
    }.associateBy { it.name }
}

data class Message(
    val sender: String,
    val receiver: String,
    val pulse: Pulse,
)


data class CycleResult(
    val finalModules: Map<String, Module>,
    val processedMessages: List<Message>
)

fun pressButtonOnce(initialModules: Map<String, Module>): CycleResult {
    data class Acc(
        val messageQueue: List<Message>,
        val modules: Map<String, Module>,
        val done: Boolean,
        val processedMessages: List<Message>
    )

    val finalState = generateSequence(
        Acc(
            listOf(Message(buttonName, broadcasterName, Pulse.Low)),
            initialModules,
            false,
            emptyList()
        )
    ) { acc ->
        val (messageQueue, modules) = acc

        if (messageQueue.isNotEmpty()) {
            val message = messageQueue.first()

            val (newMessages, updatedModule) = modules[message.receiver]?.let { receiver ->
                when (receiver) {
                    is Module.Broadcaster -> {
                        Pair(receiver.outgoingConnections.map {
                            Message(receiver.name, it, message.pulse)
                        }, null)
                    }

                    is Module.Conjunction -> receiver.updateMemory(message.sender, message.pulse).let {
                        val outgoingPulse = if (it.memory.values.all { it == Pulse.High }) Pulse.Low else Pulse.High
                        Pair(it.outgoingConnections.map { other ->
                            Message(it.name, other, outgoingPulse)
                        }, it)
                    }

                    is Module.FlipFlop -> {
                        when (message.pulse) {
                            Pulse.Low -> receiver.flip().let {
                                Pair(it.outgoingConnections.map { other ->
                                    Message(it.name, other, if (it.isOn) Pulse.High else Pulse.Low)
                                }, it)
                            }

                            Pulse.High -> Pair(emptyList(), null) // ignore pulse
                        }

                    }
                }
            } ?: Pair(emptyList(), null)

            Acc(
                messageQueue.drop(1) + newMessages,
                updatedModule?.let { modules + (it.name to it) } ?: modules,
                false,
                acc.processedMessages + message
            )
        } else {
            Acc(messageQueue, modules, true, acc.processedMessages)
        }

    }.takeWhile { !it.done }.last()

    return CycleResult(
        finalState.modules,
        finalState.processedMessages
    )
}

fun part1(file: File) {
    val lines = file.readLines()

    val initialModules = parseModules(lines)

    val (lowPulseSum, highPulseSum) = (1..1000).runningFold(CycleResult(initialModules, emptyList())) { (modules), i ->
        pressButtonOnce(modules)
    }.fold(Pair(0, 0)) { acc, cycleResult ->
        val (numberOfLowPulses, numberOfHighPulses) = cycleResult.processedMessages
            .partition { it.pulse == Pulse.Low }
            .toList()
            .map { it.count() }
        Pair(acc.first + numberOfLowPulses, acc.second + numberOfHighPulses)
    }

    println(lowPulseSum * highPulseSum)
}

// ---

const val rxModuleName = "rx"

fun part2(file: File) {
    val lines = file.readLines()

    val initialModules = parseModules(lines)

    // visualize network
    generateGraphVizGraph(initialModules, "outputs/20-graph.txt")

    // assumption: there are multiple subnetworks, which can be processed individually
    val subnetworks = splitModulesFromBroadcaster(initialModules)

    // visualize subnetworks
    subnetworks.forEachIndexed { index, modules ->
        generateGraphVizGraph(modules, "outputs/20-graph-$index.txt")
    }

    // assumption: there is a conjunction before rx
    val conjunctionModuleBeforeRx = initialModules.values
        .first { it is Module.Conjunction && it.outgoingConnections.contains(rxModuleName) }.name

    val buttonPressesPerNetwork = subnetworks.map { subnetwork ->
        generateSequence(subnetwork) { modules ->
            val (finalModules, processedMessages) = pressButtonOnce(modules)
            if (processedMessages.any { it.receiver == conjunctionModuleBeforeRx && it.pulse == Pulse.High }) {
                null
            } else {
                finalModules
            }
        }.count()
    }

    println(computeLowestCommonMultiple(buttonPressesPerNetwork))
}

fun splitModulesFromBroadcaster(modules: Map<String, Module>): List<Map<String, Module>> {
    val broadcaster = modules.values.first { it is Module.Broadcaster }
    return broadcaster.outgoingConnections.map { startModuleName ->
        data class Acc(
            val visitedModules: Set<Module>,
            val openModules: Set<Module>,
        )

        val isolatedBroadcaster = Module.Broadcaster(broadcaster.name, listOf(startModuleName))
        val visitedModules =
            generateSequence(Acc(setOf(isolatedBroadcaster), setOf(modules[startModuleName]!!))) { acc ->
                val (visitedModules, openModules) = acc
                if (openModules.isNotEmpty()) {
                    val currentModule = openModules.first()
                    if (!visitedModules.contains(currentModule)) {
                        Acc(
                            visitedModules + currentModule,
                            openModules - currentModule + currentModule.outgoingConnections.mapNotNull { modules[it] }
                        )
                    } else {
                        Acc(
                            visitedModules,
                            openModules - currentModule
                        )
                    }
                } else {
                    null
                }
            }.last().visitedModules
        visitedModules.associateBy { it.name }
    }
}

private fun generateGraphVizGraph(modules: Map<String, Module>, fileName: String) {
    val edges = modules.values.flatMap { module ->
        module.outgoingConnections.map { other ->
            "${module.name} -> $other"
        }
    }.joinToString("\n")

    val nodes = (modules.values.map {
        when (it) {
            is Module.Broadcaster -> "${it.name} [style=filled, fillcolor=blue]"
            is Module.Conjunction -> "${it.name} [style=filled, fillcolor=red, label=\"&${it.name}\"]"
            is Module.FlipFlop -> "${it.name} [style=filled, fillcolor=green, label=\"%${it.name}\"]"
        }
    } + "$rxModuleName [style=filled, fillcolor=grey]").joinToString("\n")

    val graph = "digraph {\n$nodes\n$edges\n}"

    File(fileName).writeText(graph)
}

fun main() {
    part1(File("inputs/20-part1-1.txt"))
    part1(File("inputs/20-part1-2.txt"))
    part1(File("inputs/20.txt"))
    println("---")
    part2(File("inputs/20.txt"))
}
