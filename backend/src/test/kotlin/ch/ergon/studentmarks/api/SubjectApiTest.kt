package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.api.generated.dto.DashboardAveragesDto
import ch.ergon.studentmarks.api.generated.dto.SemesterOverviewDto
import ch.ergon.studentmarks.api.generated.dto.SubjectDto
import ch.ergon.studentmarks.api.mapper.toDto
import ch.ergon.studentmarks.domain.repository.SubjectRepository
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.LernenderMarks
import ch.ergon.studentmarks.test.objects.TestSemester
import ch.ergon.studentmarks.test.objects.TestSubjects
import ch.ergon.studentmarks.test.objects.TestSubjects.Companion.SpecialTestSubjects
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import ch.ergon.studentmarks.utils.TestUuidUtilsImpl
import ch.ergon.studentmarks.utils.toDto
import ch.ergon.studentmarks.utils.toRequest
import io.restassured.RestAssured.given
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNull.nullValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SubjectApiTest : IntegrationTest() {
    @Autowired
    private lateinit var subjectRepository: SubjectRepository

    @Autowired
    private lateinit var testUuidUtilsImpl: TestUuidUtilsImpl

    @Test
    fun getAllSubjects() {
        val response =
            given()
                .withAdmin()
                .get("/api/admin/subjects")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", SubjectDto::class.java)

        assertThat(response, containsInAnyOrder(*(TestSubjects.subjects.map { it.toDto() }.toTypedArray())))
    }

    @Test
    fun addSubject() {
        testUuidUtilsImpl.setUUID(SpecialTestSubjects.AddSubject.id.value)

        val response =
            given()
                .withAdmin()
                .contentType("application/json")
                .body(SpecialTestSubjects.AddSubject.toRequest())
                .post("/api/admin/subjects")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", SubjectDto::class.java)

        assertThat(response, `is`(SpecialTestSubjects.AddSubject.toDto()))

        val subjects = subjectRepository.findAll()

        assertThat(subjects, containsInAnyOrder(*(SpecialTestSubjects.afterAddingSubject.toTypedArray())))
    }

    @Test
    fun editSubject() {
        val response =
            given()
                .withAdmin()
                .contentType("application/json")
                .body(SpecialTestSubjects.EditSubject.toRequest())
                .put("/api/admin/subjects/${SpecialTestSubjects.EditSubject.id.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", SubjectDto::class.java)

        assertThat(response, `is`(SpecialTestSubjects.EditSubject.toDto()))

        val subjects = subjectRepository.findAll()

        assertThat(subjects, containsInAnyOrder(*(SpecialTestSubjects.afterEditingSubject.toTypedArray())))
    }

    @Test
    fun getDashboardAveragesForUserInCurrentSemester() {
        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .get(
                    "/api/semesters/${TestSemester.Autumn22.id.value}/averages" +
                        "?userId=${Lernender.userId.value}",
                ).then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", DashboardAveragesDto::class.java)

        // Autumn22 marks: math1 (5500, w=1500), math2 (5500, w=1500), french2 (5000, w=1000) — all general
        assertThat(response.vocationalSubjects, hasSize(0))
        assertThat(response.generalSubjects, hasSize(2))

        val subjectNames = response.generalSubjects.map { it.subject.name }.toSet()
        assertThat(subjectNames, `is`(setOf(TestSubjects.Math.name, TestSubjects.French.name)))
    }

    @Test
    fun getAveragesForSubjectsWithoutGradedMarksReturnsNullAverage() {
        // Spring23 has: english (4750, w=1000), PastMarks.english1 (null, w=500), PastMarks.english2 (null, w=750)
        // Only English subject exists. english1 and english2 have null values.
        // But the graded english mark (4750) means English average is not null.
        // Use Autumn23 instead: FutureMark.math (null) and FutureMark.french (null) — both have null values.
        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .get(
                    "/api/semesters/${TestSemester.Autumn23.id.value}/averages" +
                        "?userId=${Lernender.userId.value}",
                ).then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", DashboardAveragesDto::class.java)

        // Autumn23 has only marks with null values, so all averages should be null
        for (subjectAvg in response.generalSubjects) {
            assertThat(subjectAvg.average.get(), nullValue())
        }
        assertThat(response.generalAverage.get(), nullValue())
    }

    @Test
    fun getSemesterOverviewWithMarksPerSubject() {
        val response =
            given()
                .withApprentice()
                .accept("application/json")
                .get(
                    "/api/semesters/${TestSemester.Autumn22.id.value}/overview" +
                        "?userId=${Lernender.userId.value}",
                ).then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", SemesterOverviewDto::class.java)

        // Autumn22 has: math1, math2 (Mathematik) and french2 (Französisch) — all general
        assertThat(response.vocationalSubjects, hasSize(0))
        assertThat(response.generalSubjects, hasSize(2))

        val mathSubject = response.generalSubjects.find { it.subject.name == TestSubjects.Math.name }!!
        assertThat(mathSubject.marks, hasSize(2))
        assertThat(
            mathSubject.marks.map { it.id },
            containsInAnyOrder(
                LernenderMarks.math1.id.value,
                LernenderMarks.math2.id.value,
            ),
        )
    }
}
