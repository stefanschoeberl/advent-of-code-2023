package dev.ssch.day24

data class Ray2D(
    val start: Vector2D,
    val direction: Vector2D,
)

data class Intersection(
    val t1: ExactFraction,
    val t2: ExactFraction,
    val point: Vector2D,
)

fun intersectRays(a: Ray2D, b: Ray2D): Intersection? {
    if (parallel(a.direction, b.direction)) {
        return null
    }
    val t1 = (det(b.direction, a.start) + det(b.start, b.direction)) / det(a.direction, b.direction)
    val t2 = (det(a.direction, b.start) + det(a.start, a.direction)) / det(b.direction, a.direction)
    return Intersection(
        t1, t2,
        a.start + a.direction * t1
    )
}
