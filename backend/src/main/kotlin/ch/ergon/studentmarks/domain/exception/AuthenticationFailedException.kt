package ch.ergon.studentmarks.domain.exception

import org.springframework.http.HttpStatus

class AuthenticationFailedException(
    code: StudentMarksExceptionCodes,
    httpStatus: HttpStatus,
    message: String?,
) : StudentMarksException(code, httpStatus, message)
