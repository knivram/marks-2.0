package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.infrastructure.security.authentication.SUBJECT_HEADER_NAME
import io.restassured.specification.RequestSpecification

@Suppress("ktlint:standard:max-line-length")
const val APPRENTICE_SUBJECT = "emailAddress%3Dmarvin.kramer%40ergon.ch%2CCN%3DMarvin%20Kramer%2COU%3DIT%2CO%3DErgon%20Informatik%20AG%2CL%3DZuerich%2CST%3DZuerich%2CC%3DCH"

@Suppress("ktlint:standard:max-line-length")
private const val SUPERVISOR_SUBJECT = "emailAddress%3Dthomas.boksberger%40ergon.ch%2CCN%3DThomas%20Boksberger%2COU%3DIT%2CO%3DErgon%20Informatik%20AG%2CL%3DZuerich%2CST%3DZuerich%2CC%3DCH"

@Suppress("ktlint:standard:max-line-length")
private const val ADMIN_SUBJECT = "emailAddress%3Dbernd.lienberger%40ergon.ch%2CCN%3DBernd%20Lienberger%2COU%3DIT%2CO%3DErgon%20Informatik%20AG%2CL%3DZuerich%2CST%3DZuerich%2CC%3DCH"

private fun RequestSpecification.setSubjectHeader(subjectString: String) = this.header(SUBJECT_HEADER_NAME, subjectString)

fun RequestSpecification.withApprentice(): RequestSpecification = this.setSubjectHeader(APPRENTICE_SUBJECT)

fun RequestSpecification.withSupervisor(): RequestSpecification = this.setSubjectHeader(SUPERVISOR_SUBJECT)

fun RequestSpecification.withAdmin(): RequestSpecification = this.setSubjectHeader(ADMIN_SUBJECT)
