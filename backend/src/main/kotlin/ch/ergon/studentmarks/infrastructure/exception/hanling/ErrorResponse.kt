package ch.ergon.studentmarks.infrastructure.exception.hanling

import ch.ergon.studentmarks.domain.exception.StudentMarksException
import ch.ergon.studentmarks.domain.exception.StudentMarksExceptionCodes

data class ErrorResponse(
    val code: StudentMarksExceptionCodes,
    val reason: String,
) {
    companion object {
        fun create(
            code: StudentMarksExceptionCodes,
            reason: String?,
        ) = ErrorResponse(code, reason ?: "No reason found")

        fun create(e: StudentMarksException) = create(e.code, e.message)
    }
}
