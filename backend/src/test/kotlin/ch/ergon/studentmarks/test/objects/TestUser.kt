package ch.ergon.studentmarks.test.objects

import ch.ergon.studentmarks.api.mapper.toDto
import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.Role
import ch.ergon.studentmarks.domain.model.user.User
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.domain.model.user.UserName

class TestUser {
    companion object {
        // Lernende
        val Lernender =
            User(
                UserId("80b5f44f-6e83-43e0-9211-9fcdfbdc5ac1"),
                UserName("mkramer"),
                Email.create("marvin.kramer@ergon.ch"),
                Role.APPRENTICE,
            )
        val Jan =
            User(
                UserId("eae33934-8c17-4f64-a36a-ce50dab9c191"),
                UserName("jsigrist"),
                Email.create("jan.sigrist@ergon.ch"),
                Role.APPRENTICE,
            )
        val Kevin =
            User(
                UserId("2bcf1294-d294-4266-b1ee-d72f6be7e847"),
                UserName("kevglatt"),
                Email.create("kevin.glatthard@ergon.ch"),
                Role.APPRENTICE,
            )
        val Fabio =
            User(
                UserId("7109e9f5-972c-4043-97b5-280e4ea1fbf3"),
                UserName("fliberto"),
                Email.create("fabio.liberto@ergon.ch"),
                Role.APPRENTICE,
            )

        // Berufsbildner
        val Berufsbildner =
            User(
                UserId("fe35edb5-c1c2-443e-8a52-90ce8cd83039"),
                UserName("boxi"),
                Email.create("thomas.boksberger@ergon.ch"),
                Role.SUPERVISOR,
            )
        val Patrick =
            User(
                UserId("e6b170aa-473c-4e3a-8442-fcaf094ec008"),
                UserName("pgruntz"),
                Email.create("patrick.gruntz@ergon.ch"),
                Role.SUPERVISOR,
            )
        val Alvaro =
            User(
                UserId("56e97688-df0e-427c-8029-35ae8d5fb9c6"),
                UserName("acatala"),
                Email.create("alvaro.catala@ergon.ch"),
                Role.SUPERVISOR,
            )
        val Noah =
            User(
                UserId("b0a715a4-78ac-4b5c-83be-29ed2f2f0839"),
                UserName("noah"),
                Email.create("noah.heusser@ergon.ch"),
                Role.SUPERVISOR,
            )
        val Tobias =
            User(
                UserId("2c2d523e-a2ef-4f72-ae97-b71ee7ae17a3"),
                UserName("tkrebs"),
                Email.create("tobias.krebs@ergon.ch"),
                Role.SUPERVISOR,
            )
        val Daniela =
            User(
                UserId("7d4db3ce-880c-4cba-bae1-7ec952d5d1ec"),
                UserName("dam"),
                Email.create("daniela.meier@ergon.ch"),
                Role.SUPERVISOR,
            )

        val Admin =
            User(
                UserId("28373b11-ad87-4fe3-994f-970c053b95d8"),
                UserName("berlie"),
                Email.create("bernd.lienberger@ergon.ch"),
                Role.ADMIN,
            )

        val UnusedUser =
            User(
                UserId("a84d99a8-ab76-4924-a137-22a3705c77f8"),
                UserName("unusedUser"),
                Email.create("unused.user@ergon.ch"),
                Role.APPRENTICE,
            )

        // Special-Users
        val DeletedUser =
            User(
                UserId("f2693531-e38d-40f8-ab78-d117f7415cd6"),
                UserName("deleted_user"),
                Email.create("deleted.user@ergon.ch"),
                Role.APPRENTICE,
            )

        // Lists

        val AllUsers =
            listOf(
                Lernender,
                Berufsbildner,
                Admin,
                Jan,
                Patrick,
                Alvaro,
                Kevin,
                Noah,
                Fabio,
                Tobias,
                Daniela,
                UnusedUser,
            )

        val UserDtos = AllUsers.map { it.toDto() }

        val Apprentices =
            listOf(
                Fabio,
                Jan,
                Kevin,
                Lernender,
                UnusedUser,
            )

        val Supervisors =
            listOf(
                Berufsbildner,
                Patrick,
                Alvaro,
                Noah,
                Tobias,
                Daniela,
            )
    }
}
