package ch.ergon.studentmarks.test.objects

import ch.ergon.studentmarks.domain.marks.SemesterId
import ch.ergon.studentmarks.domain.semester.Semester
import java.time.LocalDate
import java.util.UUID

class TestSemester {
    companion object {
        val Spring22 =
            Semester(
                SemesterId(UUID.fromString("00d94e70-18fc-49dd-bb38-99c79d8afc24")),
                "Frühlingssemester",
                LocalDate.parse("2022-02-20"),
                LocalDate.parse("2022-07-16"),
            )

        val Autumn22 =
            Semester(
                SemesterId(UUID.fromString("e9b6a70e-87cd-464b-97a6-bd15c611bcdd")),
                "Herbstsemester",
                LocalDate.parse("2022-08-21"),
                LocalDate.parse("2023-02-06"),
            )
        val Spring23 =
            Semester(
                SemesterId(UUID.fromString("66f53a81-0826-49b6-9d1a-0591617dbd63")),
                "Frühlingssemester",
                LocalDate.parse("2023-02-19"),
                LocalDate.parse("2023-07-17"),
            )
        val Autumn23 =
            Semester(
                SemesterId(UUID.fromString("50010262-4dc8-427e-b9f1-99a002fe82af")),
                "Herbstsemester",
                LocalDate.parse("2023-08-20"),
                LocalDate.parse("2024-02-05"),
            )

        val semesters =
            listOf(
                Autumn23,
                Spring23,
                Autumn22,
                Spring22,
            )

        object SpecialTestSemesters {
            val AddSemester =
                Semester(
                    SemesterId(UUID.fromString("3ca2f44f-d325-4644-a051-363603c36eef")),
                    "Herbstsemester",
                    LocalDate.parse("2021-08-22"),
                    LocalDate.parse("2022-02-05"),
                )

            val semestersAfterAdding =
                listOf(
                    *(semesters.toTypedArray()),
                    AddSemester,
                )

            val EditSemester =
                Semester(
                    SemesterId(UUID.fromString("66f53a81-0826-49b6-9d1a-0591617dbd63")),
                    "Neuer Semester Name",
                    LocalDate.parse("2023-02-19"),
                    LocalDate.parse("2023-07-17"),
                )

            val semestersAfterEditing =
                listOf(
                    Autumn23,
                    EditSemester,
                    Autumn22,
                    Spring22,
                )
        }
    }
}
