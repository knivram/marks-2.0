package ch.ergon.studentmarks.domain.exception

enum class StudentMarksExceptionCodes {
    //  REPO
    SUBJECT_NOT_FOUND,
    MARK_NOT_FOUND,
    USER_NOT_FOUND,
    USER_EXISTS,
    OWN_USER_NOT_DELETABLE,

    //  SECURITY
    NO_MAIL_IN_SUBJECT,
}
