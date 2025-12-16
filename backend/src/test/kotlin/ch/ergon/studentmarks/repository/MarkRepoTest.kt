package ch.ergon.studentmarks.repository

import ch.ergon.studentmarks.IntegrationTest
import ch.ergon.studentmarks.db.MarkRepository
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.LernenderMarks
import ch.ergon.studentmarks.test.objects.TestMarks.Companion.SpecialMarks
import ch.ergon.studentmarks.test.objects.TestSemester
import ch.ergon.studentmarks.test.objects.TestSubjects
import ch.ergon.studentmarks.test.objects.TestUser.Companion.Lernender
import ch.ergon.studentmarks.utils.TestTimeUtilsImpl
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
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
        val allMarks = markRepository.getMarks(Lernender.userId)

        assertThat(allMarks, containsInAnyOrder(*(LernenderMarks.allMarks.toTypedArray())))
    }

    @Test
    fun getAllMarksSemester() {
        val allMarks = markRepository.getMarks(Lernender.userId, semesterId = TestSemester.Spring23.id)

        assertThat(allMarks, containsInAnyOrder(*(LernenderMarks.spring23.toTypedArray())))
    }

    @Test
    fun getMarksForSubject() {
        val mathMarks =
            markRepository.getMarks(
                Lernender.userId,
                TestSemester.Autumn22.id,
                TestSubjects.Math.id,
            )

        assertThat(
            mathMarks,
            containsInAnyOrder(
                *(
                    LernenderMarks.autumn22
                        .filter { it.subject.id == TestSubjects.Math.id }
                        .toTypedArray()
                ),
            ),
        )
    }

    @Test
    fun getFutureExams() {
        val exams = markRepository.getFutureMarks(Lernender.userId, testTimeUtilsImpl.now())

        assertThat(exams, containsInAnyOrder(*(LernenderMarks.FutureMark.marks.toTypedArray())))
    }

    @Test
    fun getPassedExams() {
        val exams = markRepository.getPassedExamsWithoutValue(Lernender.userId, testTimeUtilsImpl.now())

        assertThat(exams, containsInAnyOrder(*(LernenderMarks.PastMarks.marks.toTypedArray())))
    }

    @Test
    fun getLast4ExamsWithValue() {
        val lastExams = markRepository.getPassedExamsWithValue(Lernender.userId, testTimeUtilsImpl.now(), 4)

        assertThat(lastExams, contains(*(LernenderMarks.latest4Marks.toTypedArray())))
    }

    @Test
    fun getOwnerOfMark() {
        val ownerId = markRepository.getOwnerOfMark(LernenderMarks.english.id)

        assertThat(ownerId, `is`(Lernender.userId))
    }

    @Test
    fun getOwnerOfDeletedMark() {
        val ownerId = markRepository.getOwnerOfMark(SpecialMarks.DeletedMark.id)

        assertThat(ownerId, `is`(Lernender.userId))
    }

    @Test
    fun addNewMarkLernender() {
        markRepository.newMarkForUser(Lernender, SpecialMarks.MarkForAddTest)

        val marksList =
            markRepository.getMarks(
                Lernender.userId,
                SpecialMarks.MarkForAddTest.semesterId,
                SpecialMarks.MarkForAddTest.subject.id,
            )

        assertThat(marksList, hasItem(SpecialMarks.MarkForAddTest))
    }

    @Test
    fun editMarkLernender() {
        markRepository.editMarkForUser(Lernender, SpecialMarks.EditedMathMark)
        val markList =
            markRepository.getMarks(
                Lernender.userId,
                TestSemester.Autumn22.id,
                SpecialMarks.EditedMathMark.subject.id,
            )

        assertThat(markList, hasItem(SpecialMarks.EditedMathMark))
        assertThat(markList, not(hasItem(LernenderMarks.math1)))
    }

    @Test
    fun load_deleted_marks_returns_all_deleted_marks() {
        val listOfDeletedMarks = markRepository.getDeletedMarks(Lernender.userId)

        assertThat(listOfDeletedMarks, containsInAnyOrder(SpecialMarks.DeletedMark))
    }

    @Test
    fun delete_one_mark() {
        val deleteMark = LernenderMarks.math1
        val listOfMarksBefore =
            markRepository.getMarks(Lernender.userId, TestSemester.Autumn22.id, TestSubjects.Math.id)

        assertThat(listOfMarksBefore, hasItem(LernenderMarks.math1))

        markRepository.deleteMarksBy(deleteMark.id)

        val listOfMarksAfter = markRepository.getMarks(Lernender.userId, TestSemester.Autumn22.id, TestSubjects.Math.id)

        assertThat(listOfMarksAfter, not(hasItem(LernenderMarks.math1)))
    }

    @Test
    fun delete_all_marks_of_user() {
        markRepository.deleteMarksBy(Lernender.userId)

        val marks = markRepository.getMarks(Lernender.userId)
        assertThat(marks, empty())

        val deletedMarks = markRepository.getDeletedMarks(Lernender.userId)
        assertThat(
            deletedMarks,
            containsInAnyOrder(*(LernenderMarks.allMarks.toTypedArray()), SpecialMarks.DeletedMark),
        )
    }

    @Test
    fun restore_one_mark() {
        // Restore the Mark
        markRepository.restoreMarksBy(SpecialMarks.DeletedMark.id)

        val marks =
            markRepository.getMarks(
                Lernender.userId,
                SpecialMarks.DeletedMark.semesterId,
                SpecialMarks.DeletedMark.subject.id,
            )

        assertThat(marks, containsInAnyOrder(SpecialMarks.DeletedMark))
    }
}
