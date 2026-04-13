package ch.ergon.studentmarks.domain.model.shared

data class PaginatedList<T>(
    val totalEntries: Int,
    val pageEntries: List<T>,
) : List<T> by pageEntries
