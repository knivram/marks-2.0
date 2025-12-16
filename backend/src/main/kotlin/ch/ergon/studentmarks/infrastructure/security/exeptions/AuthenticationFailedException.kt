package ch.ergon.studentmarks.infrastructure.security.exeptions

import org.springframework.http.HttpStatus

class AuthenticationFailedException(
    code: StudentMarksExceptionCodes,
    httpStatus: HttpStatus,
    message: String?,
) : StudentMarksException(code, httpStatus, message)
