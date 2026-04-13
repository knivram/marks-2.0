package ch.ergon.studentmarks.application.service

import ch.ergon.studentmarks.domain.model.mark.Mark

object AverageCalculator {
    /**
     * Weighted average for a list of marks.
     * Only marks with a non-null value are included.
     * Returns null if no graded marks exist.
     * Weight and value are stored as thousandths (e.g., 1000 = 1.0, 5500 = 5.5).
     * The returned average is in thousandths scale to match the domain convention.
     */
    fun weightedAverage(marks: List<Mark>): Double? {
        val graded = marks.filter { it.value.value != null }
        if (graded.isEmpty()) return null

        val weightedSum = graded.sumOf { it.weight.toLong() * it.value.value!!.toLong() }
        val totalWeight = graded.sumOf { it.weight.toLong() }
        if (totalWeight == 0L) return null

        return weightedSum.toDouble() / totalWeight.toDouble()
    }

    /**
     * Group-level average across multiple subject averages.
     * Simple average of subject averages (each subject counts equally).
     * Returns null if no subjects have averages.
     */
    fun groupAverage(subjectAverages: List<Double?>): Double? {
        val nonNull = subjectAverages.filterNotNull()
        if (nonNull.isEmpty()) return null
        return nonNull.average()
    }
}
