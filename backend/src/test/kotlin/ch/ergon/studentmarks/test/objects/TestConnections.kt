package ch.ergon.studentmarks.test.objects

import ch.ergon.studentmarks.api.mapper.toDto
import ch.ergon.studentmarks.application.query.model.ApprenticeSupervisorPairWithUsers
import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPair

class TestConnections {
    companion object {
        val connection1 =
            ApprenticeSupervisorPair(
                "e0466a09-cf7c-4fd2-87e3-6813544f8da8",
                TestUser.Lernender.userId,
                TestUser.Berufsbildner.userId,
                true,
            )
        val connection2 =
            ApprenticeSupervisorPair("9df6e01d-8ebf-417b-905d-06df3d3ace4d", TestUser.Jan.userId, TestUser.Alvaro.userId, false)
        val connection3 =
            ApprenticeSupervisorPair("7fbb6de7-a80c-422c-bd07-4cb5f9de8476", TestUser.Jan.userId, TestUser.Patrick.userId, false)
        val connection4 =
            ApprenticeSupervisorPair("22cdfded-3a6c-41e7-a7be-4d4371681089", TestUser.Kevin.userId, TestUser.Noah.userId, true)
        val connection5 =
            ApprenticeSupervisorPair("0abc0228-374e-41f2-9ff6-6a88c2c1089c", TestUser.Fabio.userId, TestUser.Tobias.userId, false)
        val connection6 =
            ApprenticeSupervisorPair("268a0ede-00a1-4d19-a572-2b76b7bc0c48", TestUser.Fabio.userId, TestUser.Daniela.userId, false)
        val connection7 =
            ApprenticeSupervisorPair("bf7a2036-4a53-4f68-8244-95f5b6cd538e", TestUser.Fabio.userId, TestUser.Noah.userId, false)

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

        private val userLookup = TestUser.AllUsers.associateBy { it.userId }

        fun ApprenticeSupervisorPair.withUsers(): ApprenticeSupervisorPairWithUsers =
            ApprenticeSupervisorPairWithUsers(this, userLookup.getValue(apprenticeId), userLookup.getValue(supervisorId))

        val connectionDtos = connections.map { it.withUsers().toDto() }

        val allAdminConnections =
            listOf(
                ApprenticeSupervisorPair("c0f33502-c20c-4728-a897-ccb85101532a", TestUser.Lernender.userId, TestUser.Admin.userId, false),
                ApprenticeSupervisorPair("c0f33502-c20c-4728-a897-ccb85101532a", TestUser.Jan.userId, TestUser.Admin.userId, false),
                ApprenticeSupervisorPair("c0f33502-c20c-4728-a897-ccb85101532a", TestUser.Kevin.userId, TestUser.Admin.userId, false),
                ApprenticeSupervisorPair("c0f33502-c20c-4728-a897-ccb85101532a", TestUser.Fabio.userId, TestUser.Admin.userId, false),
                ApprenticeSupervisorPair("c0f33502-c20c-4728-a897-ccb85101532a", TestUser.UnusedUser.userId, TestUser.Admin.userId, false),
            )
    }
}
