package ch.ergon.studentmarks.repository

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.db.SubjectsRepository
import ch.ergon.studentmarks.test.objects.TestSemester
import ch.ergon.studentmarks.test.objects.TestSubjects
import ch.ergon.studentmarks.test.objects.TestSubjects.Companion.SpecialTestSubjects
import ch.ergon.studentmarks.test.objects.TestSubjects.Companion.TestSubjectOverview
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.core.Is.`is`
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SubjectRepoTest : IntegrationTest() {
    @Autowired
    private lateinit var subjectsRepository: SubjectsRepository

    @Test
    fun getAllSubjects() {
        val response = subjectsRepository.getAllSubjects()

        assertThat(response, containsInAnyOrder(*(TestSubjects.subjects.toTypedArray())))
    }

    @Test
    fun addSubject() {
        val response = subjectsRepository.addSubject(SpecialTestSubjects.AddSubject)

        assertThat(response, `is`(SpecialTestSubjects.AddSubject))

        val subjects = subjectsRepository.getAllSubjects()
        assertThat(subjects, containsInAnyOrder(*(SpecialTestSubjects.afterAddingSubject.toTypedArray())))
    }

    @Test
    fun editSubject() {
        val subject = subjectsRepository.editSubject(SpecialTestSubjects.EditSubject)

        assertThat(subject, `is`(SpecialTestSubjects.EditSubject))

        val subjects = subjectsRepository.getAllSubjects()

        assertThat(subjects, containsInAnyOrder(*(SpecialTestSubjects.afterEditingSubject.toTypedArray())))
    }

    @Test
    fun get_all_subjectOverviews_of_student_and_semester() {
        val semesters = subjectsRepository.getAllSubjectsOfUser(Lernender.userId, TestSemester.Autumn22.id)

        assertThat(semesters, containsInAnyOrder(*(TestSubjectOverview.Autumn22.list.toTypedArray())))
    }

    @Test
    fun get_semesterOverview_of_with_marks_without_values_return_0_as_average() {
        val subjects = subjectsRepository.getAllSubjectsOfUser(Lernender.userId, TestSemester.Autumn23.id)

        assertThat(subjects, containsInAnyOrder(*(TestSubjectOverview.Autumn23.list.toTypedArray())))
    }

    @Test
    fun get_one_subjectOverview() {
        val subjectOverview =
            subjectsRepository.getSubject(TestSubjects.English.id, Lernender.userId, TestSemester.Spring23.id)

        assertThat(subjectOverview, `is`(TestSubjectOverview.Spring23.english))
    }
}
