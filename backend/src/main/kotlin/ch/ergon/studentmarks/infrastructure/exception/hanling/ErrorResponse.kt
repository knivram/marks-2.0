package ch.ergon.studentmarks.infrastructure.exception.hanling

import ch.ergon.studentmarks.infrastructure.security.exeptions.StudentMarksException
import ch.ergon.studentmarks.infrastructure.security.exeptions.StudentMarksExceptionCodes

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
