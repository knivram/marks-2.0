package ch.ergon.studentmarks.repository

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.domain.repository.MarkRepository
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.LernenderMarks
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.SpecialMarks
import ch.ergon.studentmarks.test.objects.TestSemester
import ch.ergon.studentmarks.test.objects.TestSubjects
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import ch.ergon.studentmarks.utils.TestTimeUtilsImpl
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class MarkRepoTest : IntegrationTest() {
    @Autowired
    private lateinit var markRepository: MarkRepository

    @Autowired
    private lateinit var testTimeUtilsImpl: TestTimeUtilsImpl

    @Test
    fun getAllMarks() {
        val allMarks = markRepository.findByUser(Lernender.userId)

        assertThat(allMarks, containsInAnyOrder(*(LernenderMarks.allMarks.toTypedArray())))
    }

    @Test
    fun getAllMarksSemester() {
        val allMarks = markRepository.findByUser(Lernender.userId, semesterId = TestSemester.Spring23.id)

        assertThat(allMarks, containsInAnyOrder(*(LernenderMarks.spring23.toTypedArray())))
    }

    @Test
    fun getMarksForSubject() {
        val mathMarks =
            markRepository.findByUser(
                Lernender.userId,
                TestSemester.Autumn22.id,
                TestSubjects.Math.id,
            )

        assertThat(
            mathMarks,
            containsInAnyOrder(
                *(
                    LernenderMarks.autumn22
                        .filter { it.subjectId == TestSubjects.Math.id }
                        .toTypedArray()
                ),
            ),
        )
    }

    @Test
    fun getUpcomingExams() {
        // Default time is 2023-05-31. FutureMark.marks have dates 2023-12-02 and 2024-01-24.
        val upcomingExams = markRepository.findUpcomingExams(Lernender.userId, testTimeUtilsImpl.now())

        assertThat(
            upcomingExams.map { it.mark },
            containsInAnyOrder(*(LernenderMarks.FutureMark.marks.toTypedArray())),
        )
    }

    @Test
    fun getPastExamsWithoutGrade() {
        // Default time is 2023-05-31. PastMarks have date 2023-04-04 and null values.
        val pastExams = markRepository.findPastExamsWithoutGrade(Lernender.userId, testTimeUtilsImpl.now())

        assertThat(
            pastExams.map { it.mark },
            containsInAnyOrder(*(LernenderMarks.PastMarks.marks.toTypedArray())),
        )
    }

    @Test
    fun getOwnerOfMark() {
        val ownerId = markRepository.getOwnerUserId(LernenderMarks.english.id)

        assertThat(ownerId, `is`(Lernender.userId))
    }

    @Test
    fun getOwnerOfDeletedMark() {
        val ownerId = markRepository.getOwnerUserId(SpecialMarks.DeletedMark.id)

        assertThat(ownerId, `is`(Lernender.userId))
    }

    @Test
    fun addNewMarkLernender() {
        markRepository.save(Lernender.userId, SpecialMarks.MarkForAddTest)

        val marksList =
            markRepository.findByUser(
                Lernender.userId,
                SpecialMarks.MarkForAddTest.semesterId,
                SpecialMarks.MarkForAddTest.subjectId,
            )

        assertThat(marksList, hasItem(SpecialMarks.MarkForAddTest))
    }

    @Test
    fun editMarkLernender() {
        markRepository.update(Lernender.userId, SpecialMarks.EditedMathMark)
        val markList =
            markRepository.findByUser(
                Lernender.userId,
                TestSemester.Autumn22.id,
                SpecialMarks.EditedMathMark.subjectId,
            )

        assertThat(markList, hasItem(SpecialMarks.EditedMathMark))
        assertThat(markList, not(hasItem(LernenderMarks.math1)))
    }

    @Test
    fun load_deleted_marks_returns_all_deleted_marks() {
        val listOfDeletedMarks = markRepository.findDeleted(Lernender.userId)

        assertThat(listOfDeletedMarks, containsInAnyOrder(SpecialMarks.DeletedMark))
    }

    @Test
    fun delete_one_mark() {
        val deleteMark = LernenderMarks.math1
        val listOfMarksBefore =
            markRepository.findByUser(Lernender.userId, TestSemester.Autumn22.id, TestSubjects.Math.id)

        assertThat(listOfMarksBefore, hasItem(LernenderMarks.math1))

        markRepository.softDelete(deleteMark.id)

        val listOfMarksAfter = markRepository.findByUser(Lernender.userId, TestSemester.Autumn22.id, TestSubjects.Math.id)

        assertThat(listOfMarksAfter, not(hasItem(LernenderMarks.math1)))
    }

    @Test
    fun delete_all_marks_of_user() {
        markRepository.softDeleteByUser(Lernender.userId)

        val marks = markRepository.findByUser(Lernender.userId)
        assertThat(marks, empty())

        val deletedMarks = markRepository.findDeleted(Lernender.userId)
        assertThat(
            deletedMarks,
            containsInAnyOrder(*(LernenderMarks.allMarks.toTypedArray()), SpecialMarks.DeletedMark),
        )
    }

    @Test
    fun restore_one_mark() {
        // Restore the Mark
        markRepository.restore(SpecialMarks.DeletedMark.id)

        val marks =
            markRepository.findByUser(
                Lernender.userId,
                SpecialMarks.DeletedMark.semesterId,
                SpecialMarks.DeletedMark.subjectId,
            )

        assertThat(marks, containsInAnyOrder(SpecialMarks.DeletedMark))
    }
}
