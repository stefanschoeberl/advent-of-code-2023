package dev.ssch.day24

import java.io.File

data class Point3D(
    val x: Long,
    val y: Long,
    val z: Long,
) {
    fun toVector2DXY(): Vector2D {
        return Vector2D(
            x.toExactFraction(),
            y.toExactFraction()
        )
    }
}

data class Hailstone(
    val position: Point3D,
    val velocity: Point3D,
) {
    fun toRay2DXY(): Ray2D {
        return Ray2D(
            position.toVector2DXY(),
            velocity.toVector2DXY()
        )
    }
}

fun parsePoint3D(text: String): Point3D {
    val (x, y, z) = text.split(",")
        .map { it.trim().toLong() }
    return Point3D(x, y, z)
}

fun parseHailstone(text: String): Hailstone {
    val (position, velocity) = text.split("@")
        .map { parsePoint3D(it.trim()) }
    return Hailstone(position, velocity)
}

operator fun LongRange.contains(number: ExactFraction): Boolean {
    return number >= this.first.toExactFraction() && number <= this.last.toExactFraction()
}

fun doHailstonesCollide2DXY(a: Hailstone, b: Hailstone, xyRange: LongRange): Boolean {
    return intersectRays(a.toRay2DXY(), b.toRay2DXY())?.let {
        it.t1 > 0.toExactFraction() && it.t2 > 0.toExactFraction() && it.point.x in xyRange && it.point.y in xyRange
    } ?: false
}

fun <T> Iterable<T>.pairs(): Iterable<Pair<T, T>> {
    return this.flatMapIndexed { index: Int, a: T ->
        this.drop(index + 1).map { b -> Pair(a, b) }
    }
}

fun part1(file: File, xyRange: LongRange) {
    val lines = file.readLines()
    val hailstones = lines.map { parseHailstone(it) }
    val amount = hailstones.pairs().count { doHailstonesCollide2DXY(it.first, it.second, xyRange) }
    println(amount)
}

// ---

fun computeCoefficientsFromHailstones(a: Hailstone, b: Hailstone, selectorX: (Point3D) -> Long, selectorY: (Point3D) -> Long): List<ExactFraction> {
    return listOf(
        selectorY(b.velocity).toExactFraction() - selectorY(a.velocity).toExactFraction(),
        selectorX(a.velocity).toExactFraction() - selectorX(b.velocity).toExactFraction(),
        selectorY(a.position).toExactFraction() - selectorY(b.position).toExactFraction(),
        selectorX(b.position).toExactFraction() - selectorX(a.position).toExactFraction()
    )
}

fun computeVectorComponentFromHailstones(a: Hailstone, b: Hailstone, selectorX: (Point3D) -> Long, selectorY: (Point3D) -> Long): ExactFraction {
    return selectorY(a.position).toExactFraction() * selectorX(a.velocity).toExactFraction() -
            selectorY(b.position).toExactFraction() * selectorX(b.velocity).toExactFraction() -
            selectorX(a.position).toExactFraction() * selectorY(a.velocity).toExactFraction() +
            selectorX(b.position).toExactFraction() * selectorY(b.velocity).toExactFraction()
}

fun solveEquations(hailstones: List<Hailstone>, selectorX: (Point3D) -> Long, selectorY: (Point3D) -> Long): Vector4D {
    val hailstonePairs = hailstones.pairs().take(4)
    val matrix = hailstonePairs.map { (a, b) ->
        computeCoefficientsFromHailstones(a, b, selectorX, selectorY)
    }.let { Matrix4D(it) }
    val vector = hailstonePairs
        .map { (a, b) -> computeVectorComponentFromHailstones(a, b, selectorX, selectorY) }
        .let { (x, y, z, w) -> Vector4D(x, y, z, w) }
    return matrix.invert()!! * vector
}

fun part2(file: File) {
    val lines = file.readLines()
    val hailstones = lines.map { parseHailstone(it) }

    val (startX, startY, velocityX, velocityY) = solveEquations(hailstones, { it.x }, { it.y })
    val (_, startZ, _, velocityZ) = solveEquations(hailstones, { it.x }, { it.z })

    val sum = startX + startY + startZ
    println(sum.toBigDecimal128())
}

fun main() {
    part1(File("inputs/24-part1.txt"), 7L..27L)
    part1(File("inputs/24.txt"), 200000000000000L..400000000000000L)
    println("---")
    part2(File("inputs/24-part1.txt"))
    part2(File("inputs/24.txt"))
}
