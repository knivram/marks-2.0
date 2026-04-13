package ch.ergon.studentmarks.repository

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.domain.repository.SubjectRepository
import ch.ergon.studentmarks.test.objects.TestSubjects
import ch.ergon.studentmarks.test.objects.TestSubjects.Companion.SpecialTestSubjects
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SubjectRepoTest : IntegrationTest() {
    @Autowired
    private lateinit var subjectRepository: SubjectRepository

    @Test
    fun getAllSubjects() {
        val response = subjectRepository.findAll()

        assertThat(response, containsInAnyOrder(*(TestSubjects.subjects.toTypedArray())))
    }

    @Test
    fun addSubject() {
        val response = subjectRepository.save(SpecialTestSubjects.AddSubject)

        assertThat(response, `is`(SpecialTestSubjects.AddSubject))

        val subjects = subjectRepository.findAll()
        assertThat(subjects, containsInAnyOrder(*(SpecialTestSubjects.afterAddingSubject.toTypedArray())))
    }

    @Test
    fun editSubject() {
        val subject = subjectRepository.update(SpecialTestSubjects.EditSubject)

        assertThat(subject, `is`(SpecialTestSubjects.EditSubject))

        val subjects = subjectRepository.findAll()

        assertThat(subjects, containsInAnyOrder(*(SpecialTestSubjects.afterEditingSubject.toTypedArray())))
    }

    // TODO: getAllSubjectsOfUser and getSubject methods no longer exist on SubjectRepository.
    // SubjectOverview has been removed. Use SemesterOverviewQueryService instead.
    // @Test
    // fun get_all_subjectOverviews_of_student_and_semester() { ... }

    // @Test
    // fun get_semesterOverview_of_with_marks_without_values_return_0_as_average() { ... }

    // @Test
    // fun get_one_subjectOverview() { ... }
}
