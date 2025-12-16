package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.api.subjects.FullSubjectDto
import ch.ergon.studentmarks.api.subjects.SubjectOverviewDto
import ch.ergon.studentmarks.db.SubjectsRepository
import ch.ergon.studentmarks.domain.subjects.Subject
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.LernenderMarks
import ch.ergon.studentmarks.test.objects.TestSemester
import ch.ergon.studentmarks.test.objects.TestSubjects
import ch.ergon.studentmarks.test.objects.TestSubjects.Companion.SpecialTestSubjects
import ch.ergon.studentmarks.test.objects.TestSubjects.Companion.TestSubjectOverview
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import ch.ergon.studentmarks.utils.TestUuidUtilsImpl
import ch.ergon.studentmarks.utils.toRequest
import io.restassured.RestAssured.given
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SubjectApiTest : IntegrationTest() {
    @Autowired
    private lateinit var subjectsRepository: SubjectsRepository

    @Autowired
    private lateinit var testUuidUtilsImpl: TestUuidUtilsImpl

    @Test
    fun getAllSubjects() {
        val response =
            given()
                .withApprentice()
                .get("/api/subjects/all")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", Subject::class.java)

        assertThat(response, containsInAnyOrder(*(TestSubjects.subjects.toTypedArray())))
    }

    @Test
    fun addSubject() {
        testUuidUtilsImpl.setUUID(SpecialTestSubjects.AddSubject.id.value)

        val response =
            given()
                .withAdmin()
                .contentType("application/json")
                .body(SpecialTestSubjects.AddSubject.toRequest())
                .post("/api/subjects")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", Subject::class.java)

        assertThat(response, `is`(SpecialTestSubjects.AddSubject))

        val subjects = subjectsRepository.getAllSubjects()

        assertThat(subjects, containsInAnyOrder(*(SpecialTestSubjects.afterAddingSubject.toTypedArray())))
    }

    @Test
    fun editSubject() {
        val response =
            given()
                .withAdmin()
                .contentType("application/json")
                .body(SpecialTestSubjects.EditSubject.toRequest())
                .put("/api/subjects/${SpecialTestSubjects.EditSubject.id.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", Subject::class.java)

        assertThat(response, `is`(SpecialTestSubjects.EditSubject))

        val subjects = subjectsRepository.getAllSubjects()

        assertThat(subjects, containsInAnyOrder(*(SpecialTestSubjects.afterEditingSubject.toTypedArray())))
    }

    @Test
    fun get_subjectOverviews_for_user_in_current_semester() {
        val response =
            given()
                .withApprentice()
                .get("/api/subjects?semesterId=${TestSemester.Autumn22.id.value}&userId=${Lernender.userId.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", SubjectOverviewDto::class.java)

        assertThat(response, containsInAnyOrder(*(TestSubjectOverview.Autumn22.listDto.toTypedArray())))
    }

    @Test
    fun get_semesterOverview_of_with_marks_without_values_return_0_as_average() {
        val response =
            given()
                .withApprentice()
                .get("/api/subjects?semesterId=${TestSemester.Autumn23.id.value}&userId=${Lernender.userId.value}")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", SubjectOverviewDto::class.java)

        assertThat(response, containsInAnyOrder(*(TestSubjectOverview.Autumn23.listDto.toTypedArray())))
    }

    @Test
    fun getFullSubject() {
        val response =
            given()
                .withApprentice()
                .get(
                    "/api/subjects/${TestSubjects.English.id.value}/full" +
                        "?semesterId=${TestSemester.Spring23.id.value}" +
                        "&userId=${Lernender.userId.value}",
                ).then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getObject(".", FullSubjectDto::class.java)

        val expectedFullSubjectDto =
            FullSubjectDto(
                TestSubjectOverview.Spring23.english.subjectId.value,
                TestSubjectOverview.Spring23.english.name,
                TestSubjectOverview.Spring23.english.isVocationalModule,
                TestSubjectOverview.Spring23.english.description,
                TestSubjectOverview.Spring23.english.markAverage.value,
                LernenderMarks.spring23.sortedBy { it.date },
            )

        assertThat(response, `is`(expectedFullSubjectDto))
    }
}
