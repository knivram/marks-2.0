package ch.ergon.studentmarks.application.service

import ch.ergon.studentmarks.application.query.model.MarkWithSubject
import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.MarkRepository
import ch.ergon.studentmarks.domain.repository.SubjectRepository
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
    private val subjectRepository: SubjectRepository,
    private val userService: UserService,
    private val timeUtils: TimeUtils,
    private val emailNotificationService: EmailNotificationService,
) {
    fun addNewMark(mark: Mark): MarkWithSubject {
        val apprentice = userService.getCurrentUser()
        val newMark = markRepository.save(apprentice.userId, mark)
        val subject = subjectRepository.findById(newMark.subjectId)

        // Calculate average for the email notification
        val allMarksForSubject = markRepository.findByUser(apprentice.userId, newMark.semesterId, newMark.subjectId)
        val average =
            ch.ergon.studentmarks.application.query.AverageCalculator
                .weightedAverage(allMarksForSubject)

        emailNotificationService.sendAddMarkEmail(
            apprentice = apprentice,
            mark = newMark,
            subjectName = subject.name,
            averageValue = average?.toInt() ?: 0,
        )
        return MarkWithSubject(newMark, subject)
    }

    @PreAuthorize("hasPermission(#mark.id, 'IS_OWNER_OF')")
    fun editMark(mark: Mark): MarkWithSubject {
        val user = userService.getCurrentUser()
        val oldMark = markRepository.findById(mark.id)
        val oldSubject = subjectRepository.findById(oldMark.subjectId)

        val oldMarksForSubject = markRepository.findByUser(user.userId, oldMark.semesterId, oldMark.subjectId)
        val oldAverage =
            ch.ergon.studentmarks.application.query.AverageCalculator
                .weightedAverage(oldMarksForSubject)

        val newMark = markRepository.update(user.userId, mark)
        val newSubject = subjectRepository.findById(newMark.subjectId)

        val newMarksForSubject = markRepository.findByUser(user.userId, newMark.semesterId, newMark.subjectId)
        val newAverage =
            ch.ergon.studentmarks.application.query.AverageCalculator
                .weightedAverage(newMarksForSubject)

        if (newMark.value !== oldMark.value) {
            emailNotificationService.sendEditMarkEmail(
                apprentice = user,
                newMark = newMark,
                oldMark = oldMark,
                newSubjectName = newSubject.name,
                oldSubjectName = oldSubject.name,
                oldAverageValue = oldAverage?.toInt() ?: 0,
                newAverageValue = newAverage?.toInt() ?: 0,
            )
        }
        return MarkWithSubject(newMark, newSubject)
    }

    @PreAuthorize("hasPermission(#markId, 'IS_OWNER_OF') or hasRole('ROLE_ADMIN')")
    fun deleteMark(markId: MarkId) {
        markRepository.softDelete(markId)
    }

    @PreAuthorize("hasPermission(#markId, 'IS_OWNER_OF') or hasRole('ROLE_ADMIN')")
    fun restoreMark(markId: MarkId): MarkWithSubject {
        val mark = markRepository.restore(markId)
        val subject = subjectRepository.findById(mark.subjectId)
        return MarkWithSubject(mark, subject)
    }

    @PreAuthorize("hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')")
    fun calculate(
        userId: UserId,
        semesterId: SemesterId,
        subjectId: SubjectId,
        goal: MarkValue,
        weight: Int,
    ): MarkValue {
        val allMarks = markRepository.findByUser(userId, semesterId, subjectId).filter { it.value.value != null }
        var sumOfAllWeights = 0.0
        var sumWeightedValues = 0.0

        for (mark in allMarks) {
            val currentWeight = mark.weight / 1000.0
            val currentValue = mark.value.value!! / 1000.0
            sumOfAllWeights += currentWeight
            sumWeightedValues += currentWeight * currentValue
        }

        val goalWeight = weight / 1000.0
        val goalTotalMarks = (sumOfAllWeights + goalWeight) * (goal.value!! / 1000.0)
        val neededMark = (goalTotalMarks - sumWeightedValues) / goalWeight

        return MarkValue((neededMark * 1000).toInt())
    }

    @Scheduled(fixedRate = 3_600_000)
    fun deleteAllExpiredMarks() {
        val expiredTimeEnd = LocalDateTime.now().minusMinutes(30)
        markRepository.deleteExpiredSoftDeleted(expiredTimeEnd)
    }
}
