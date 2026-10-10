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
        val response = subjectRepository.getAll()

        assertThat(response, containsInAnyOrder(*(TestSubjects.subjects.toTypedArray())))
    }

    @Test
    fun addSubject() {
        val response = subjectRepository.save(SpecialTestSubjects.AddSubject)

        assertThat(response, `is`(SpecialTestSubjects.AddSubject))

        val subjects = subjectRepository.getAll()
        assertThat(subjects, containsInAnyOrder(*(SpecialTestSubjects.afterAddingSubject.toTypedArray())))
    }

    @Test
    fun editSubject() {
        val subject = subjectRepository.update(SpecialTestSubjects.EditSubject)

        assertThat(subject, `is`(SpecialTestSubjects.EditSubject))

        val subjects = subjectRepository.getAll()

        assertThat(subjects, containsInAnyOrder(*(SpecialTestSubjects.afterEditingSubject.toTypedArray())))
    }
}
