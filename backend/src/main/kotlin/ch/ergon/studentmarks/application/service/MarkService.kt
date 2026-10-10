package ch.ergon.studentmarks.application.service

import ch.ergon.studentmarks.application.query.MarkQueryRepository
import ch.ergon.studentmarks.application.query.MarkView
import ch.ergon.studentmarks.application.response.Agenda
import ch.ergon.studentmarks.application.response.DashboardAverages
import ch.ergon.studentmarks.application.response.SemesterOverview
import ch.ergon.studentmarks.application.response.SubjectAverage
import ch.ergon.studentmarks.application.response.SubjectWithMarks
import ch.ergon.studentmarks.application.security.HasMarkCreatePermission
import ch.ergon.studentmarks.application.security.HasMarkDeletePermission
import ch.ergon.studentmarks.application.security.HasMarkEditPermission
import ch.ergon.studentmarks.application.security.HasMarkPurgePermission
import ch.ergon.studentmarks.application.security.HasStudentDataReadPermission
import ch.ergon.studentmarks.application.security.TargetId
import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.repository.MarkRepository
import ch.ergon.studentmarks.domain.repository.SubjectRepository
import ch.ergon.studentmarks.utils.TimeUtils
import ch.ergon.studentmarks.utils.UuidUtils
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class MarkService(
    private val markRepository: MarkRepository,
    private val markQueryRepository: MarkQueryRepository,
    private val subjectRepository: SubjectRepository,
    private val userService: UserService,
    private val timeUtils: TimeUtils,
    private val emailNotificationService: EmailNotificationService,
    private val uuidUtils: UuidUtils,
) {
    @HasMarkCreatePermission
    fun addNewMark(
        @TargetId userId: UserId,
        name: String,
        subjectId: SubjectId,
        date: LocalDate,
        weight: Int,
        value: MarkValue,
        semesterId: SemesterId,
    ): MarkView {
        val apprentice = userService.getUser(userId)
        val mark =
            Mark(
                id = MarkId(uuidUtils.randomUUID()),
                name = name,
                subjectId = subjectId,
                date = date,
                weight = weight,
                value = value,
                semesterId = semesterId,
            )
        val newMark = markRepository.save(apprentice.userId, mark)
        val markView = markQueryRepository.getById(newMark.id)

        val allMarksForSubject = markRepository.findByUser(apprentice.userId, newMark.semesterId, newMark.subjectId)
        val average = AverageCalculator.weightedAverage(allMarksForSubject)

        emailNotificationService.sendAddMarkEmail(
            apprentice = apprentice,
            mark = newMark,
            subjectName = markView.subject.name,
            averageValue = average?.toInt() ?: 0,
        )
        return markView
    }

    @HasMarkEditPermission
    fun editMark(
        @TargetId markId: MarkId,
        name: String,
        subjectId: SubjectId,
        date: LocalDate,
        weight: Int,
        value: MarkValue,
        semesterId: SemesterId,
    ): MarkView {
        // TODO: Consider optimizing the calls here!
        val oldMark = markRepository.getById(markId)
        val markOwnerId = markRepository.getOwnerUserId(markId)
        val apprentice = userService.getUser(markOwnerId)
        val oldSubject = subjectRepository.getById(oldMark.subjectId)

        val oldMarksForSubject = markRepository.findByUser(apprentice.userId, oldMark.semesterId, oldMark.subjectId)
        val oldAverage = AverageCalculator.weightedAverage(oldMarksForSubject)

        val mark =
            oldMark.copy(
                name = name,
                subjectId = subjectId,
                date = date,
                weight = weight,
                value = value,
                semesterId = semesterId,
            )
        val newMark = markRepository.update(apprentice.userId, mark)
        val newMarkView = markQueryRepository.getById(newMark.id)

        val newMarksForSubject = markRepository.findByUser(apprentice.userId, newMark.semesterId, newMark.subjectId)
        val newAverage = AverageCalculator.weightedAverage(newMarksForSubject)

        if (newMark.value != oldMark.value) {
            // TODO: Consider using spring events and moving full calculation into the notification service
            emailNotificationService.sendEditMarkEmail(
                apprentice = apprentice,
                newMark = newMark,
                oldMark = oldMark,
                newSubjectName = newMarkView.subject.name,
                oldSubjectName = oldSubject.name,
                oldAverageValue = oldAverage?.toInt() ?: 0,
                newAverageValue = newAverage?.toInt() ?: 0,
            )
        }
        return newMarkView
    }

    @HasMarkDeletePermission
    fun deleteMark(
        @TargetId markId: MarkId,
    ) {
        markRepository.softDelete(markId)
    }

    @HasMarkDeletePermission
    fun restoreMark(
        @TargetId markId: MarkId,
    ): MarkView {
        val mark = markRepository.restore(markId)
        return markQueryRepository.getById(mark.id)
    }

    @HasStudentDataReadPermission
    fun calculate(
        @TargetId userId: UserId,
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

    @HasStudentDataReadPermission
    fun getAgenda(
        @TargetId userId: UserId,
    ): Agenda =
        Agenda(
            pastExamsWithoutGrade = markQueryRepository.findPastExamsWithoutGrade(userId, timeUtils.now()),
            upcomingExams = markQueryRepository.findUpcomingExams(userId, timeUtils.now()),
        )

    @HasStudentDataReadPermission
    fun getSemesterOverview(
        @TargetId userId: UserId,
        semesterId: SemesterId,
    ): SemesterOverview {
        val markViews = markQueryRepository.findByUser(userId, semesterId)
        val bySubject = markViews.groupBy { it.subject }

        val subjectViews =
            bySubject.map { (subject, items) ->
                SubjectWithMarks(
                    subject = subject,
                    average = AverageCalculator.weightedAverage(items.map { it.toMark() }),
                    marks = items,
                )
            }

        val (vocational, general) = subjectViews.partition { it.subject.isVocationalModule }

        return SemesterOverview(
            vocationalSubjects = vocational,
            generalSubjects = general,
            vocationalAverage = AverageCalculator.groupAverage(vocational.map { it.average }),
            generalAverage = AverageCalculator.groupAverage(general.map { it.average }),
        )
    }

    @HasStudentDataReadPermission
    fun getDashboardAverages(
        @TargetId userId: UserId,
        semesterId: SemesterId,
    ): DashboardAverages {
        val markViews = markQueryRepository.findByUser(userId, semesterId)
        val bySubject = markViews.groupBy { it.subject }

        val subjectAverages =
            bySubject.map { (subject, items) ->
                SubjectAverage(
                    subject = subject,
                    average = AverageCalculator.weightedAverage(items.map { it.toMark() }),
                )
            }

        val (vocational, general) = subjectAverages.partition { it.subject.isVocationalModule }

        return DashboardAverages(
            vocationalSubjects = vocational,
            generalSubjects = general,
            vocationalAverage = AverageCalculator.groupAverage(vocational.map { it.average }),
            generalAverage = AverageCalculator.groupAverage(general.map { it.average }),
        )
    }

    @HasMarkPurgePermission
    // TODO: Use cron annotation and put it into application.yaml
    @Scheduled(fixedRate = 3_600_000)
    fun deleteAllExpiredMarks() {
        val expiredTimeEnd = LocalDateTime.now().minusMinutes(30)
        markRepository.deleteExpiredSoftDeleted(expiredTimeEnd)
    }
}
