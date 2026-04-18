package ch.ergon.studentmarks.api

import ch.ergon.studentmarks.api.generated.UsersApi
import ch.ergon.studentmarks.api.generated.dto.UserDto
import ch.ergon.studentmarks.api.mapper.toDto
import ch.ergon.studentmarks.application.service.UserService
import ch.ergon.studentmarks.domain.model.user.UserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class UserController(
    private val userService: UserService,
) : UsersApi {
    override fun userInfo(): ResponseEntity<UserDto> {
        val user = userService.getCurrentUser()
        return ResponseEntity.ok(user.toDto())
    }

    override fun getApprentice(id: UUID): ResponseEntity<List<UserDto>> {
        val apprentices = userService.getApprentice(UserId(id))
        return ResponseEntity.ok(apprentices.map { it.toDto() })
    }
}
