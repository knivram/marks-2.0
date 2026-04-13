package ch.ergon.studentmarks.test.objects

import ch.ergon.studentmarks.domain.model.mark.Mark
import ch.ergon.studentmarks.domain.model.mark.MarkId
import ch.ergon.studentmarks.domain.model.mark.MarkValue
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import java.time.LocalDate
import java.util.UUID

class TestMarks {
    companion object {
        object LernenderMarks {
            val french1 =
                Mark(
                    MarkId(UUID.fromString("087db115-3b23-46ca-b44d-8ef4eca28965")),
                    "Voci-Test",
                    TestSubjects.French.id,
                    LocalDate.parse("2022-04-20"),
                    1000,
                    MarkValue(5000),
                    SemesterId("00d94e70-18fc-49dd-bb38-99c79d8afc24"),
                )
            val french2 =
                Mark(
                    MarkId(UUID.fromString("fb8fefeb-a29d-4589-8329-1578c53567e0")),
                    "Voci-Test 2",
                    TestSubjects.French.id,
                    LocalDate.parse("2022-12-11"),
                    1000,
                    MarkValue(5000),
                    SemesterId("e9b6a70e-87cd-464b-97a6-bd15c611bcdd"),
                )
            val math1 =
                Mark(
                    MarkId(UUID.fromString("bf8ef84a-c5c6-4933-ad85-50809fe8eb49")),
                    "Sin, Cos, Tan",
                    TestSubjects.Math.id,
                    LocalDate.parse("2022-10-23"),
                    1500,
                    MarkValue(5500),
                    SemesterId("e9b6a70e-87cd-464b-97a6-bd15c611bcdd"),
                )
            val math2 =
                Mark(
                    MarkId(UUID.fromString("31c9909c-f0dd-425a-892d-c44791342874")),
                    "Addition und Subtraktion",
                    TestSubjects.Math.id,
                    LocalDate.parse("2022-12-23"),
                    1500,
                    MarkValue(5500),
                    SemesterId("e9b6a70e-87cd-464b-97a6-bd15c611bcdd"),
                )
            val english =
                Mark(
                    MarkId(UUID.fromString("35e683bc-4051-4b97-80ab-7f50588c0611")),
                    "Past Simple",
                    TestSubjects.English.id,
                    LocalDate.parse("2023-05-01"),
                    1000,
                    MarkValue(4750),
                    SemesterId("66f53a81-0826-49b6-9d1a-0591617dbd63"),
                )

            val allMarks =
                listOf(
                    french1,
                    french2,
                    math1,
                    math2,
                    english,
                    *(FutureMark.marks.toTypedArray()),
                    *(PastMarks.marks.toTypedArray()),
                )
            val latest5Marks = listOf(english, math2, math1, french2, french1).sortedByDescending { it.date }
            val latest4Marks = listOf(english, math2, math1, french2).sortedByDescending { it.date }

            val spring22 = listOf(french1)
            val autumn22 = listOf(math1, math2, french2)
            val spring23 = listOf(english, PastMarks.english1, PastMarks.english2)
            val autumn23 = listOf(FutureMark.math, FutureMark.french)

            object FutureMark {
                val french =
                    Mark(
                        MarkId(UUID.fromString("14223a88-a262-4347-9dc8-d176c7fd9c7e")),
                        "Französisch Verben",
                        TestSubjects.French.id,
                        LocalDate.parse("2024-01-24"),
                        1250,
                        MarkValue(null),
                        SemesterId("50010262-4dc8-427e-b9f1-99a002fe82af"),
                    )

                val math =
                    Mark(
                        MarkId(UUID.fromString("684df94c-d874-45bb-ae7f-da3e019ec58c")),
                        "Kurzen von Brüchen",
                        TestSubjects.Math.id,
                        LocalDate.parse("2023-12-02"),
                        1000,
                        MarkValue(null),
                        SemesterId("50010262-4dc8-427e-b9f1-99a002fe82af"),
                    )

                val marks = listOf(french, math)
            }

            object PastMarks {
                val english1 =
                    Mark(
                        MarkId(UUID.fromString("1861801a-7861-4851-96a3-81d71a7a65fe")),
                        "Vocitest",
                        TestSubjects.English.id,
                        LocalDate.parse("2023-04-04"),
                        500,
                        MarkValue(null),
                        SemesterId("66f53a81-0826-49b6-9d1a-0591617dbd63"),
                    )

                val english2 =
                    Mark(
                        MarkId(UUID.fromString("f41fa713-afb0-48bc-9357-3182726e50be")),
                        "Future",
                        TestSubjects.English.id,
                        LocalDate.parse("2023-04-04"),
                        750,
                        MarkValue(null),
                        SemesterId("66f53a81-0826-49b6-9d1a-0591617dbd63"),
                    )

                val marks = listOf(english1, english2)
            }
        }

        object UnusedUserMarks {
            val french =
                Mark(
                    MarkId("11e032f5-5b03-4df3-b78f-b6efda42275c"),
                    "Voci Test",
                    TestSubjects.French.id,
                    LocalDate.parse("2022-04-20"),
                    1000,
                    MarkValue(5000),
                    SemesterId("00d94e70-18fc-49dd-bb38-99c79d8afc24"),
                )
            val math =
                Mark(
                    MarkId("2d7fa523-43d2-4c3e-a4c6-0fd0317d7c14"),
                    "Sin, Cos, Tan",
                    TestSubjects.Math.id,
                    LocalDate.parse("2022-10-23"),
                    1500,
                    MarkValue(5500),
                    SemesterId("e9b6a70e-87cd-464b-97a6-bd15c611bcdd"),
                )
            val english =
                Mark(
                    MarkId("a838e911-ec43-48c6-8fd9-be079d6ff333"),
                    "Past Simple",
                    TestSubjects.English.id,
                    LocalDate.parse("2023-05-01"),
                    1000,
                    MarkValue(4750),
                    SemesterId("66f53a81-0826-49b6-9d1a-0591617dbd63"),
                )
            val marks = listOf(french, math, english)
        }

        object SpecialMarks {
            // Wird in den Tests zur DB hinzugefügt
            val MarkForAddTest =
                Mark(
                    MarkId("aeee5d61-89db-4ce1-9667-9b669ca14188"),
                    "New Mark",
                    TestSubjects.English.id,
                    LocalDate.parse("2023-01-10"),
                    1000,
                    MarkValue(5450),
                    SemesterId("e9b6a70e-87cd-464b-97a6-bd15c611bcdd"),
                )

            // Editierte Version einer bestehenden Note
            val EditedMathMark =
                Mark(
                    MarkId(UUID.fromString("bf8ef84a-c5c6-4933-ad85-50809fe8eb49")),
                    "Sin, Cos",
                    TestSubjects.Math.id,
                    LocalDate.parse("2022-10-23"),
                    1250,
                    MarkValue(4500),
                    SemesterId("e9b6a70e-87cd-464b-97a6-bd15c611bcdd"),
                )

            // Eine schon gelöschte Note
            val DeletedMark =
                Mark(
                    MarkId(UUID.fromString("9727e907-5374-46a1-a4d3-91a5518d41ac")),
                    "deleted_mark",
                    TestSubjects.Math.id,
                    LocalDate.parse("2023-06-02"),
                    500,
                    MarkValue(5000),
                    SemesterId("66f53a81-0826-49b6-9d1a-0591617dbd63"),
                )
        }
    }
}
