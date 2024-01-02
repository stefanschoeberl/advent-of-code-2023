package dev.ssch.day24

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.text.DecimalFormat

data class ExactFraction(
    val numerator: BigInteger,
    val denominator: BigInteger,
) {

    fun toBigDecimal128(): BigDecimal {
        return numerator.toBigDecimal().divide(denominator.toBigDecimal(), MathContext.DECIMAL128)
    }

    operator fun times(other: ExactFraction): ExactFraction {
        return ExactFraction(
            this.numerator * other.numerator,
            this.denominator * other.denominator
        ).simplify()
    }

    operator fun div(other: ExactFraction): ExactFraction {
        return ExactFraction(
            this.numerator * other.denominator,
            this.denominator * other.numerator
        ).simplify()
    }

    operator fun plus(other: ExactFraction): ExactFraction {
        return ExactFraction(
            this.numerator * other.denominator + this.denominator * other.numerator,
            this.denominator * other.denominator
        ).simplify()
    }

    operator fun minus(other: ExactFraction): ExactFraction {
        return ExactFraction(
            this.numerator * other.denominator - this.denominator * other.numerator,
            this.denominator * other.denominator
        ).simplify()
    }

    operator fun compareTo(other: ExactFraction): Int {
        return (this.numerator * other.denominator).compareTo(this.denominator * other.numerator)
    }

    private fun simplify(): ExactFraction {
        val gcd = numerator.gcd(denominator)
        return if (gcd != BigInteger.ONE) {
            ExactFraction(
                numerator / gcd,
                denominator / gcd
            )
        } else {
            this
        }.let {
            if (it.denominator < BigInteger.ZERO) {
                ExactFraction(
                    it.numerator.negate(),
                    it.denominator.abs()
                )
            } else {
                it
            }
        }
    }

    override fun toString(): String {
        return "${this.numerator}/${this.denominator} ~ ${
            DecimalFormat("#0.####").format(this.toBigDecimal128())}"
    }
}

fun Int.toExactFraction(): ExactFraction {
    return ExactFraction(this.toBigInteger(), BigInteger.ONE)
}

fun Long.toExactFraction(): ExactFraction {
    return ExactFraction(this.toBigInteger(), BigInteger.ONE)
}
