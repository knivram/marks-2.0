package ch.ergon.studentmarks.api.mapper

import ch.ergon.studentmarks.api.generated.dto.AgendaDto
import ch.ergon.studentmarks.api.generated.dto.DashboardAveragesDto
import ch.ergon.studentmarks.api.generated.dto.MarkDto
import ch.ergon.studentmarks.api.generated.dto.MarkValueDto
import ch.ergon.studentmarks.api.generated.dto.SemesterOverviewDto
import ch.ergon.studentmarks.api.generated.dto.SubjectAverageDto
import ch.ergon.studentmarks.api.generated.dto.SubjectWithMarksDto
import ch.ergon.studentmarks.application.query.MarkView
import ch.ergon.studentmarks.application.response.Agenda
import ch.ergon.studentmarks.application.response.DashboardAverages
import ch.ergon.studentmarks.application.response.SemesterOverview
import ch.ergon.studentmarks.application.response.SubjectAverage
import ch.ergon.studentmarks.application.response.SubjectWithMarks
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import org.openapitools.jackson.nullable.JsonNullable

fun MarkView.toDto(): MarkDto =
    MarkDto(
        id = id.value,
        name = name,
        subject = subject.toDto(),
        date = date,
        weight = weight,
        value = value.value,
        semesterId = semesterId.value,
    )

fun MarkValue.toDto(): MarkValueDto = MarkValueDto(value = value)

fun SemesterOverview.toDto(): SemesterOverviewDto =
    SemesterOverviewDto(
        vocationalSubjects = vocationalSubjects.map { it.toDto() },
        generalSubjects = generalSubjects.map { it.toDto() },
        vocationalAverage = JsonNullable.of(vocationalAverage),
        generalAverage = JsonNullable.of(generalAverage),
    )

fun SubjectWithMarks.toDto(): SubjectWithMarksDto =
    SubjectWithMarksDto(
        subject = subject.toDto(),
        average = JsonNullable.of(average),
        marks = marks.map { it.toDto() },
    )

fun DashboardAverages.toDto(): DashboardAveragesDto =
    DashboardAveragesDto(
        vocationalSubjects = vocationalSubjects.map { it.toDto() },
        generalSubjects = generalSubjects.map { it.toDto() },
        vocationalAverage = JsonNullable.of(vocationalAverage),
        generalAverage = JsonNullable.of(generalAverage),
    )

fun SubjectAverage.toDto(): SubjectAverageDto =
    SubjectAverageDto(
        subject = subject.toDto(),
        average = JsonNullable.of(average),
    )

fun Agenda.toDto(): AgendaDto =
    AgendaDto(
        pastExamsWithoutGrade = pastExamsWithoutGrade.map { it.toDto() },
        upcomingExams = upcomingExams.map { it.toDto() },
    )
