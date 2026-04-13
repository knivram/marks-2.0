package ch.ergon.studentmarks.domain.exception

import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.UserName
import ch.ergon.studentmarks.domain.exception.StudentMarksExceptionCodes.MARK_NOT_FOUND
import ch.ergon.studentmarks.domain.exception.StudentMarksExceptionCodes.NO_MAIL_IN_SUBJECT
import ch.ergon.studentmarks.domain.exception.StudentMarksExceptionCodes.NO_SUBJECT
import ch.ergon.studentmarks.domain.exception.StudentMarksExceptionCodes.OWN_USER_NOT_DELETABLE
import ch.ergon.studentmarks.domain.exception.StudentMarksExceptionCodes.SUBJECT_NOT_FOUND
import ch.ergon.studentmarks.domain.exception.StudentMarksExceptionCodes.USER_EXISTS
import ch.ergon.studentmarks.domain.exception.StudentMarksExceptionCodes.USER_NOT_FOUND
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNAUTHORIZED

open class StudentMarksException(
    val code: StudentMarksExceptionCodes,
    val httpStatus: HttpStatus,
    message: String? = null,
) : RuntimeException(message)

class MarkNotFoundException(
    markId: MarkId,
) : StudentMarksException(
        MARK_NOT_FOUND,
        NOT_FOUND,
        "The mark (${markId.value}) was not found",
    )

class SubjectNotFoundException(
    val subjectId: SubjectId,
) : StudentMarksException(
        SUBJECT_NOT_FOUND,
        NOT_FOUND,
        "The subject (${subjectId.value}) was not found",
    )

class UserNotFoundException(
    val searchParam: String,
    override val message: String = "The user could not be found",
) : StudentMarksException(
        USER_NOT_FOUND,
        NOT_FOUND,
        message,
    )

class UserAlreadyExistsException(
    val email: Email,
    val userName: UserName,
) : StudentMarksException(
        USER_EXISTS,
        CONFLICT,
        "A user with ether this email ($email) or this username ($userName) already exists",
    )

class DeleteOwnUserException :
    StudentMarksException(
        OWN_USER_NOT_DELETABLE,
        BAD_REQUEST,
        "The own user can not be deleted",
    )

class SubjectNotInHeaderException :
    StudentMarksException(
        NO_SUBJECT,
        UNAUTHORIZED,
        "There was no subject in the request",
    )

class MailNotInSubjectException :
    StudentMarksException(
        NO_MAIL_IN_SUBJECT,
        INTERNAL_SERVER_ERROR,
        "There was no mail in the given subject",
    )
