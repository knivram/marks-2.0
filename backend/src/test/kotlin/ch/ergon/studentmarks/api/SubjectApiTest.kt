package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.api.generated.dto.SubjectDto
import ch.ergon.studentmarks.api.mapper.toDto
import ch.ergon.studentmarks.domain.model.subject.Subject
import ch.ergon.studentmarks.domain.repository.SubjectRepository
import ch.ergon.studentmarks.test.objects.TestSubjects
import ch.ergon.studentmarks.test.objects.TestSubjects.Companion.SpecialTestSubjects
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

    // TODO: Old endpoint /api/subjects?semesterId=...&userId=... no longer exists.
    // SubjectOverview and SubjectOverviewDto have been removed.
    // Rewrite to use /api/semesters/{semesterId}/overview or /api/semesters/{semesterId}/averages.
    // @Test
    // fun get_subjectOverviews_for_user_in_current_semester() { ... }

    // TODO: Old endpoint /api/subjects?semesterId=...&userId=... no longer exists.
    // @Test
    // fun get_semesterOverview_of_with_marks_without_values_return_0_as_average() { ... }

    // TODO: Old endpoint /api/subjects/{id}/full no longer exists.
    // FullSubjectDto has been removed.
    // @Test
    // fun getFullSubject() { ... }
}
