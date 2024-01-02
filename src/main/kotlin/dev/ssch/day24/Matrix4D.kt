package dev.ssch.day24

data class Matrix4D(
    val values: List<List<ExactFraction>>,
) {

    companion object {
        val unity = matrixOf(listOf(
            listOf(1, 0, 0, 0),
            listOf(0, 1, 0, 0),
            listOf(0, 0, 1, 0),
            listOf(0, 0, 0, 1)
        ))
    }

    fun invert(): Matrix4D? {
        val resultValues = unity.values.mutableCopy()
        val currentValues = values.mutableCopy()

        val lastIndex = currentValues.indices.last

        for (row in 0..lastIndex) {
            // find row where values[?][row] != 0 and swap with this row
            val swapRow = currentValues.drop(row).indexOfFirst { it[row] != 0.toExactFraction() } + row
            currentValues.swapRows(row, swapRow)
            resultValues.swapRows(row, swapRow)

            // divide row by value on diagonal
            val divisor = currentValues[row][row]

            // if value on diagonal is zero => matrix is not invertible
            if (divisor == 0.toExactFraction()) {
                return null
            }
            for (col in 0..lastIndex) {
                currentValues[row][col] /= divisor
                resultValues[row][col] /= divisor
            }

            // subtract current row from rows below
            for (otherRow in (row+1)..lastIndex) {
                val multiplier = currentValues[otherRow][row]
                for (col in 0..lastIndex) {
                    currentValues[otherRow][col] -= multiplier * currentValues[row][col]
                    resultValues[otherRow][col] -= multiplier * resultValues[row][col]
                }
            }
        }

        for (row in lastIndex downTo 0) {
            // subtract current row from rows above
            for (otherRow in 0..<row) {
                val multiplier = currentValues[otherRow][row]
                for (col in 0..lastIndex) {
                    currentValues[otherRow][col] -= multiplier * currentValues[row][col]
                    resultValues[otherRow][col] -= multiplier * resultValues[row][col]
                }
            }
        }

        return Matrix4D(resultValues)
    }

    operator fun times(vector: Vector4D): Vector4D {
        val vectorAsList = listOf(vector.x, vector.y, vector.z, vector.w)
        val (x, y, z, w) = values.map {
            it.zip(vectorAsList)
                .map { (a, b) -> a * b }
                .reduce { a, b -> a + b }
        }
        return Vector4D(x, y, z, w)
    }
}

fun matrixOf(values: List<List<Int>>): Matrix4D {
    return Matrix4D(values.toExactFraction())
}

fun Matrix4D.print() {
    this.values.forEach { row ->
        println(row.joinToString(" ") { it.toString().padStart(20) })
    }
}

private fun List<List<Int>>.toExactFraction(): List<List<ExactFraction>> {
    return this.map { row -> row.map { it.toExactFraction() } }
}

private fun List<List<ExactFraction>>.mutableCopy(): List<MutableList<ExactFraction>> {
    return this.map { it.toMutableList() }
}

private fun List<MutableList<ExactFraction>>.swapRows(a: Int, b: Int) {
    val rowA = this[a]
    val rowB = this[b]
    for (col in rowA.indices) {
        val temp = rowA[col]
        rowA[col] = rowB[col]
        rowB[col] = temp
    }
}
