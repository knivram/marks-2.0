package ch.ergon.studentmarks.api.semester

data class PaginatedListDto<T>(
    val totalEntries: Number,
    val offset: Number,
    val list: List<T>,
)
