package ch.ergon.studentmarks.application.service

import ch.ergon.studentmarks.application.response.Agenda
import ch.ergon.studentmarks.application.response.DashboardAverages
import ch.ergon.studentmarks.application.response.MarkWithSubject
import ch.ergon.studentmarks.application.response.SemesterOverview
import ch.ergon.studentmarks.application.response.SubjectAverage
import ch.ergon.studentmarks.application.response.SubjectWithMarks
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
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@EnableScheduling
class MarkService(
    private val markRepository: MarkRepository,
    private val subjectRepository: SubjectRepository,
    private val userService: UserService,
    private val timeUtils: TimeUtils,
    private val emailNotificationService: EmailNotificationService,
    private val uuidUtils: UuidUtils,
) {
    fun addNewMark(
        name: String,
        subjectId: SubjectId,
        date: LocalDate,
        weight: Int,
        value: MarkValue,
        semesterId: SemesterId,
    ): MarkWithSubject {
        val apprentice = userService.getCurrentUser()
        val mark = Mark(
            id = MarkId(uuidUtils.randomUUID()),
            name = name,
            subjectId = subjectId,
            date = date,
            weight = weight,
            value = value,
            semesterId = semesterId,
        )
        val newMark = markRepository.save(apprentice.userId, mark)
        val subject = subjectRepository.getById(newMark.subjectId)

        val allMarksForSubject = markRepository.findByUser(apprentice.userId, newMark.semesterId, newMark.subjectId)
        val average = AverageCalculator.weightedAverage(allMarksForSubject)

        emailNotificationService.sendAddMarkEmail(
            apprentice = apprentice,
            mark = newMark,
            subjectName = subject.name,
            averageValue = average?.toInt() ?: 0,
        )
        return MarkWithSubject(newMark, subject)
    }

    @PreAuthorize("hasPermission(#markId, 'IS_OWNER_OF')")
    fun editMark(
        markId: MarkId,
        name: String,
        subjectId: SubjectId,
        date: LocalDate,
        weight: Int,
        value: MarkValue,
        semesterId: SemesterId,
    ): MarkWithSubject {
        val user = userService.getCurrentUser()
        val oldMark = markRepository.getById(markId)
        val oldSubject = subjectRepository.getById(oldMark.subjectId)

        val oldMarksForSubject = markRepository.findByUser(user.userId, oldMark.semesterId, oldMark.subjectId)
        val oldAverage = AverageCalculator.weightedAverage(oldMarksForSubject)

        val mark = oldMark.copy(
            name = name,
            subjectId = subjectId,
            date = date,
            weight = weight,
            value = value,
            semesterId = semesterId,
        )
        val newMark = markRepository.update(user.userId, mark)
        val newSubject = subjectRepository.getById(newMark.subjectId)

        val newMarksForSubject = markRepository.findByUser(user.userId, newMark.semesterId, newMark.subjectId)
        val newAverage = AverageCalculator.weightedAverage(newMarksForSubject)

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
        val subject = subjectRepository.getById(mark.subjectId)
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

    @PreAuthorize(
        "hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')",
    )
    fun getAgenda(userId: UserId): Agenda =
        Agenda(
            pastExamsWithoutGrade = markRepository.findPastExamsWithoutGrade(userId, timeUtils.now()),
            upcomingExams = markRepository.findUpcomingExams(userId, timeUtils.now()),
        )

    @PreAuthorize(
        "hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')",
    )
    fun getSemesterOverview(
        userId: UserId,
        semesterId: SemesterId,
    ): SemesterOverview {
        val marksWithSubjects = markRepository.findWithSubjects(userId, semesterId)
        val bySubject = marksWithSubjects.groupBy { it.subject }

        val subjectViews =
            bySubject.map { (subject, items) ->
                val marks = items.map { it.mark }
                SubjectWithMarks(
                    subject = subject,
                    average = AverageCalculator.weightedAverage(marks),
                    marks = marks,
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

    @PreAuthorize(
        "hasPermission(#userId, 'IS_CURRENT_USER') or hasPermission(#userId, 'IS_SUPERVISOR_OF') or hasRole('ROLE_ADMIN')",
    )
    fun getDashboardAverages(
        userId: UserId,
        semesterId: SemesterId,
    ): DashboardAverages {
        val marksWithSubjects = markRepository.findWithSubjects(userId, semesterId)
        val bySubject = marksWithSubjects.groupBy { it.subject }

        val subjectAverages =
            bySubject.map { (subject, items) ->
                SubjectAverage(
                    subject = subject,
                    average = AverageCalculator.weightedAverage(items.map { it.mark }),
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

    @Scheduled(fixedRate = 3_600_000)
    fun deleteAllExpiredMarks() {
        val expiredTimeEnd = LocalDateTime.now().minusMinutes(30)
        markRepository.deleteExpiredSoftDeleted(expiredTimeEnd)
    }
}
