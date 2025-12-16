package ch.ergon.studentmarks.test.objects

import ch.ergon.studentmarks.api.toDto
import ch.ergon.studentmarks.domain.user.ApprenticeSupervisorPair

class TestConnections {
    companion object {
        val connection1 =
            ApprenticeSupervisorPair(
                "e0466a09-cf7c-4fd2-87e3-6813544f8da8",
                TestUser.Lernender,
                TestUser.Berufsbildner,
                true,
            )
        val connection2 =
            ApprenticeSupervisorPair("9df6e01d-8ebf-417b-905d-06df3d3ace4d", TestUser.Jan, TestUser.Alvaro, false)
        val connection3 =
            ApprenticeSupervisorPair("7fbb6de7-a80c-422c-bd07-4cb5f9de8476", TestUser.Jan, TestUser.Patrick, false)
        val connection4 =
            ApprenticeSupervisorPair("22cdfded-3a6c-41e7-a7be-4d4371681089", TestUser.Kevin, TestUser.Noah, true)
        val connection5 =
            ApprenticeSupervisorPair("0abc0228-374e-41f2-9ff6-6a88c2c1089c", TestUser.Fabio, TestUser.Tobias, false)
        val connection6 =
            ApprenticeSupervisorPair("268a0ede-00a1-4d19-a572-2b76b7bc0c48", TestUser.Fabio, TestUser.Daniela, false)
        val connection7 =
            ApprenticeSupervisorPair("bf7a2036-4a53-4f68-8244-95f5b6cd538e", TestUser.Fabio, TestUser.Noah, false)

        val connections =
            listOf(
                connection1,
                connection2,
                connection3,
                connection4,
                connection5,
                connection6,
                connection7,
            )

        val connectionDtos = connections.map { it.toDto() }

        val allAdminConnections =
            listOf(
                ApprenticeSupervisorPair("c0f33502-c20c-4728-a897-ccb85101532a", TestUser.Lernender, TestUser.Admin, false),
                ApprenticeSupervisorPair("c0f33502-c20c-4728-a897-ccb85101532a", TestUser.Jan, TestUser.Admin, false),
                ApprenticeSupervisorPair("c0f33502-c20c-4728-a897-ccb85101532a", TestUser.Kevin, TestUser.Admin, false),
                ApprenticeSupervisorPair("c0f33502-c20c-4728-a897-ccb85101532a", TestUser.Fabio, TestUser.Admin, false),
                ApprenticeSupervisorPair("c0f33502-c20c-4728-a897-ccb85101532a", TestUser.UnusedUser, TestUser.Admin, false),
            )
    }
}
