package ch.ergon.studentmarks.test.objects

import ch.ergon.studentmarks.domain.model.subject.Subject
import ch.ergon.studentmarks.domain.model.subject.SubjectId
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

        // TestSubjectOverview removed — SubjectOverview class no longer exists.
        // Tests that used it should use the new /api/semesters/{id}/overview or /api/semesters/{id}/averages endpoints.
    }
}
