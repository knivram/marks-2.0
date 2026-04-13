package ch.ergon.studentmarks.api.mapper

import ch.ergon.studentmarks.api.generated.dto.AgendaDto
import ch.ergon.studentmarks.api.generated.dto.DashboardAveragesDto
import ch.ergon.studentmarks.api.generated.dto.MarkDto
import ch.ergon.studentmarks.api.generated.dto.MarkValueDto
import ch.ergon.studentmarks.api.generated.dto.MarkWithSubjectDto
import ch.ergon.studentmarks.api.generated.dto.SemesterOverviewDto
import ch.ergon.studentmarks.api.generated.dto.SubjectAverage1Dto
import ch.ergon.studentmarks.api.generated.dto.SubjectWithMarks1Dto
import ch.ergon.studentmarks.application.query.model.Agenda
import ch.ergon.studentmarks.application.query.model.DashboardAverages
import ch.ergon.studentmarks.application.query.model.MarkWithSubject
import ch.ergon.studentmarks.application.query.model.SemesterOverview
import ch.ergon.studentmarks.application.query.model.SubjectAverage
import ch.ergon.studentmarks.application.query.model.SubjectWithMarks
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import org.openapitools.jackson.nullable.JsonNullable

fun MarkWithSubject.toDto(): MarkWithSubjectDto =
    MarkWithSubjectDto(
        mark =
            MarkDto(
                id = mark.id.value,
                name = mark.name,
                subject = subject.toDto(),
                date = mark.date,
                weight = mark.weight,
                value = mark.value.value,
                semesterId = mark.semesterId.value,
            ),
        subject = subject.toDto(),
    )

fun MarkValue.toDto(): MarkValueDto = MarkValueDto(value = value)

fun SemesterOverview.toDto(): SemesterOverviewDto =
    SemesterOverviewDto(
        vocationalSubjects = vocationalSubjects.map { it.toDto() },
        generalSubjects = generalSubjects.map { it.toDto() },
        vocationalAverage = JsonNullable.of(vocationalAverage),
        generalAverage = JsonNullable.of(generalAverage),
    )

fun SubjectWithMarks.toDto(): SubjectWithMarks1Dto =
    SubjectWithMarks1Dto(
        subject = subject.toDto(),
        average = JsonNullable.of(average),
        marks =
            marks.map { mark ->
                MarkDto(
                    id = mark.id.value,
                    name = mark.name,
                    subject = subject.toDto(),
                    date = mark.date,
                    weight = mark.weight,
                    value = mark.value.value,
                    semesterId = mark.semesterId.value,
                )
            },
    )

fun DashboardAverages.toDto(): DashboardAveragesDto =
    DashboardAveragesDto(
        vocationalSubjects = vocationalSubjects.map { it.toDto() },
        generalSubjects = generalSubjects.map { it.toDto() },
        vocationalAverage = JsonNullable.of(vocationalAverage),
        generalAverage = JsonNullable.of(generalAverage),
    )

fun SubjectAverage.toDto(): SubjectAverage1Dto =
    SubjectAverage1Dto(
        subject = subject.toDto(),
        average = JsonNullable.of(average),
    )

fun Agenda.toDto(): AgendaDto =
    AgendaDto(
        pastExamsWithoutGrade = pastExamsWithoutGrade.map { it.toDto() },
        upcomingExams = upcomingExams.map { it.toDto() },
    )
