package ch.ergon.studentmarks.application.security

import ch.ergon.studentmarks.domain.model.user.UserPermission

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@GrantedTo(usersWithAnyOf = [UserPermission.STUDENT_DATA_READ])
annotation class HasStudentDataReadPermission

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@GrantedTo(usersWithAnyOf = [UserPermission.APPRENTICE_READ])
annotation class HasApprenticeReadPermission

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@GrantedTo(usersWithAnyOf = [UserPermission.MARK_CREATE])
annotation class HasMarkCreatePermission

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@GrantedTo(usersWithAnyOf = [UserPermission.MARK_EDIT])
annotation class HasMarkEditPermission

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@GrantedTo(usersWithAnyOf = [UserPermission.MARK_DELETE])
annotation class HasMarkDeletePermission

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@GrantedTo(admins = true)
annotation class IsAdmin

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@GrantedTo(technicalActorsWithAnyOf = [TechnicalPermission.MARK_PURGE])
annotation class HasMarkPurgePermission

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@GrantedTo(technicalActorsWithAnyOf = [TechnicalPermission.NOTIFICATION_SEND])
annotation class HasNotificationSendPermission
