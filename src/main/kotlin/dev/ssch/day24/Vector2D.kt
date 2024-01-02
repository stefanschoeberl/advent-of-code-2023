package dev.ssch.day24

data class Vector2D(
    val x: ExactFraction,
    val y: ExactFraction,
) {

    operator fun plus(other: Vector2D): Vector2D {
        return Vector2D(
            this.x + other.x,
            this.y + other.y
        )
    }

    operator fun minus(other: Vector2D): Vector2D {
        return Vector2D(
            this.x - other.x,
            this.y - other.y
        )
    }

    operator fun times(other: ExactFraction): Vector2D {
        return Vector2D(
            this.x * other,
            this.y * other
        )
    }
}

fun det(a: Vector2D, b: Vector2D): ExactFraction {
    return a.x * b.y - a.y * b.x
}

fun parallel(a: Vector2D, b: Vector2D): Boolean {
    return a.x / b.x == a.y / b.y
}
