package ch.ergon.studentmarks.test.objects

import ch.ergon.studentmarks.api.toDto
import ch.ergon.studentmarks.domain.marks.MarkAverage
import ch.ergon.studentmarks.domain.marks.SubjectId
import ch.ergon.studentmarks.domain.subjects.Subject
import ch.ergon.studentmarks.domain.subjects.SubjectOverview
import java.util.UUID

class TestSubjects {
    companion object {
        val French =
            Subject(
                SubjectId(UUID.fromString("09f526bc-24ba-4ed9-988b-bac98d670112")),
                "Französisch",
                false,
                null,
            )
        val Math =
            Subject(
                SubjectId(UUID.fromString("37bad6f5-461a-4d50-9074-02c95879148f")),
                "Mathematik",
                false,
                null,
            )
        val English =
            Subject(
                SubjectId(UUID.fromString("c0b7270d-49c6-4ab2-af0f-af3371745fed")),
                "Englisch",
                false,
                null,
            )
        val M117 =
            Subject(
                SubjectId(UUID.fromString("0f7ed446-a8b6-4bd5-aed6-4260579b6c18")),
                "M117",
                true,
                "Informatik- und Netzinfrastruktur für ein kleines Unternehmen realisieren",
            )

        val subjects = listOf(French, Math, English, M117)

        object SpecialTestSubjects {
            val EditSubject =
                Subject(
                    SubjectId(UUID.fromString("0f7ed446-a8b6-4bd5-aed6-4260579b6c18")),
                    "new Name",
                    true,
                    null,
                )
            val afterEditingSubject = listOf(French, Math, English, EditSubject)

            val AddSubject =
                Subject(
                    SubjectId(UUID.fromString("94b07ca6-ef2e-492b-b8e1-ce2cbd93a50b")),
                    "neues Fach",
                    false,
                    "Beschreibung eines neuen Faches.",
                )
            val afterAddingSubject = listOf(French, Math, English, M117, AddSubject)
        }

        object TestSubjectOverview {
            object Spring22 {
                val french =
                    SubjectOverview(
                        SubjectId(UUID.fromString("09f526bc-24ba-4ed9-988b-bac98d670112")),
                        "Französisch",
                        false,
                        null,
                        MarkAverage(5000),
                    )
            }

            object Autumn22 {
                val math =
                    SubjectOverview(
                        SubjectId(UUID.fromString("37bad6f5-461a-4d50-9074-02c95879148f")),
                        "Mathematik",
                        false,
                        null,
                        MarkAverage(5500),
                    )
                val french =
                    SubjectOverview(
                        SubjectId(UUID.fromString("09f526bc-24ba-4ed9-988b-bac98d670112")),
                        "Französisch",
                        false,
                        null,
                        MarkAverage(5000),
                    )

                val list = listOf(math, french)
                val listDto = list.map { it.toDto() }
            }

            object Spring23 {
                val english =
                    SubjectOverview(
                        SubjectId(UUID.fromString("c0b7270d-49c6-4ab2-af0f-af3371745fed")),
                        "Englisch",
                        false,
                        null,
                        MarkAverage(4750),
                    )
            }

            object Autumn23 {
                val math =
                    SubjectOverview(
                        SubjectId(UUID.fromString("37bad6f5-461a-4d50-9074-02c95879148f")),
                        "Mathematik",
                        false,
                        null,
                        MarkAverage(0),
                    )
                val french =
                    SubjectOverview(
                        SubjectId(UUID.fromString("09f526bc-24ba-4ed9-988b-bac98d670112")),
                        "Französisch",
                        false,
                        null,
                        MarkAverage(0),
                    )
                val list = listOf(math, french)
                val listDto = list.map { it.toDto() }
            }
        }
    }
}
