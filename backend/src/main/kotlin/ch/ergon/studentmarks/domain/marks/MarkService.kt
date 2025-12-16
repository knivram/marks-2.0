package ch.ergon.studentmarks.domain.marks

import ch.ergon.studentmarks.db.MarkRepository
import ch.ergon.studentmarks.db.SubjectsRepository
import ch.ergon.studentmarks.domain.email.EmailService
import ch.ergon.studentmarks.domain.user.UserId
import ch.ergon.studentmarks.domain.user.UserService
import ch.ergon.studentmarks.utils.TimeUtils
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
@EnableScheduling
class MarkService(
    private val markRepository: MarkRepository,
    private val userService: UserService,
    private val timeUtils: TimeUtils,
    private val emailService: EmailService,
    private val subjectsRepository: SubjectsRepository,
) {
    @PreAuthorize("hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')")
    fun getAllMarks(
        semesterId: SemesterId,
        userId: UserId,
        subjectId: SubjectId? = null,
    ): List<Mark> = markRepository.getMarks(userId, semesterId, subjectId)

    @PreAuthorize("hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')")
    fun getFutureExams(userId: UserId): List<Mark> = markRepository.getFutureMarks(userId, timeUtils.now())

    @PreAuthorize("hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')")
    fun getPassedExams(userId: UserId): List<Mark> = markRepository.getPassedExamsWithoutValue(userId, timeUtils.now())

    fun addNewMark(mark: Mark): Mark {
        val apprentice = userService.getCurrentUser()
        val newMark = markRepository.newMarkForUser(apprentice, mark)
        val newSubjectOverview =
            subjectsRepository.getSubject(newMark.subject.id, apprentice.userId, newMark.semesterId)
        emailService.sendAddMarkEmailHtml(apprentice, newMark, newSubjectOverview.markAverage)
        return newMark
    }

    @PreAuthorize("hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF')")
    fun getPassedMarksWithValue(
        userId: UserId,
        limit: Int,
    ): List<Mark> = markRepository.getPassedExamsWithValue(userId, timeUtils.now(), limit)

    @PreAuthorize("hasPermission(#mark.id, 'IS_OWNER_OF')")
    fun editMark(mark: Mark): Mark {
        val user = userService.getCurrentUser()
        val oldMark = markRepository.getMark(mark.id)
        val oldSubjectOverview = subjectsRepository.getSubject(oldMark.subject.id, user.userId, oldMark.semesterId)

        val newMark = markRepository.editMarkForUser(user, mark)
        val newSubjectOverview = subjectsRepository.getSubject(newMark.subject.id, user.userId, newMark.semesterId)

        if (newMark.value !== oldMark.value) {
            emailService.sendEditMarkEmail(
                userService.getCurrentUser(),
                newMark = newMark,
                oldMark = oldMark,
                oldAverage = oldSubjectOverview.markAverage,
                newAverage = newSubjectOverview.markAverage,
            )
        }
        return newMark
    }

    @PreAuthorize("hasPermission(#markId, 'IS_OWNER_OF') or hasRole('ROLE_ADMIN')")
    fun deleteMark(markId: MarkId) {
        markRepository.deleteMarksBy(markId)
    }

    @PreAuthorize("hasPermission(#markId, 'IS_OWNER_OF') or hasRole('ROLE_ADMIN')")
    fun restoreMark(markId: MarkId): Mark = markRepository.restoreMarksBy(markId)

    @PreAuthorize("hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')")
    fun calculate(
        userId: UserId,
        semesterId: SemesterId,
        subjectId: SubjectId,
        goal: MarkValue,
        weight: Int,
    ): MarkValue {
        val allMarks = markRepository.getMarks(userId, semesterId, subjectId).filter { it.value.value != null }
        var sumOfAllWeights = 0.0
        var sumWeightedValues = 0.0

        for (mark in allMarks) {
            val currentWeight =
                mark.weight / 1000.0 // Weight of the mark in the correct scale (e.g. 0.75 instead of 750)
            val currentValue =
                mark.value.value!! / 1000.0 // Value of the mark in the correct scaling (e.g. 6.0 instead of 6000)

            sumOfAllWeights += currentWeight // Cumulative weight of previous grades
            sumWeightedValues += currentWeight * currentValue // Cumulative sum of value times weight of previous grades
        }

        val goalWeight = weight / 1000.0 // Weight of the target note in the correct scale
        val goalTotalMarks =
            (sumOfAllWeights + goalWeight) * (goal.value!! / 1000.0) // Total score for the target grade in the correct scale

        val neededMark = (goalTotalMarks - sumWeightedValues) / goalWeight // Required grade to achieve the goal

        return MarkValue((neededMark * 1000).toInt()) // Return the grade as 1000 times (e.g. 6000 instead of 6.0)
    }

    @Scheduled(fixedRate = 3_600_000)
    fun deleteAllExpiredMarks() {
        val expiredTimeEnd = LocalDateTime.now().minusMinutes(30)
        markRepository.deleteExpiredRemovedMark(expiredTimeEnd)
    }
}
