package ch.ergon.studentmarks.infrastructure.exception.hanling

import ch.ergon.studentmarks.infrastructure.security.exeptions.AuthenticationFailedException
import ch.ergon.studentmarks.infrastructure.security.exeptions.StudentMarksException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(value = [StudentMarksException::class, AuthenticationFailedException::class])
    protected fun handleConflict(
        e: StudentMarksException,
        request: WebRequest?,
    ): ResponseEntity<Any>? {
        val bodyOfResponse = ErrorResponse.create(e)
        return ResponseEntity(bodyOfResponse, e.httpStatus)
    }
}
