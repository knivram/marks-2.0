package ch.ergon.studentmarks.domain

data class PaginatedList<T>(
    val totalEntries: Int,
    val pageEntries: List<T>,
) : List<T> by pageEntries
