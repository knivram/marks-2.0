package ch.ergon.studentmarks.api.admin

import ch.ergon.studentmarks.api.generated.AdminApi
import ch.ergon.studentmarks.api.generated.dto.ApprenticeConnectRequestDto
import ch.ergon.studentmarks.api.generated.dto.ApprenticeSupervisorPairDto
import ch.ergon.studentmarks.api.generated.dto.PaginatedSemesterListDto
import ch.ergon.studentmarks.api.generated.dto.SemesterDto
import ch.ergon.studentmarks.api.generated.dto.SemesterRequestDto
import ch.ergon.studentmarks.api.generated.dto.SimpleUserDto
import ch.ergon.studentmarks.api.generated.dto.SubjectDto
import ch.ergon.studentmarks.api.generated.dto.SubjectRequestDto
import ch.ergon.studentmarks.api.generated.dto.UserDto
import ch.ergon.studentmarks.api.mapper.toDomain
import ch.ergon.studentmarks.api.mapper.toDto
import ch.ergon.studentmarks.api.mapper.toPaginatedSemesterListDto
import ch.ergon.studentmarks.application.service.SemesterService
import ch.ergon.studentmarks.application.service.SubjectService
import ch.ergon.studentmarks.application.service.UserService
import ch.ergon.studentmarks.domain.model.semester.SemesterId
import ch.ergon.studentmarks.domain.model.shared.PaginationParams
import ch.ergon.studentmarks.domain.model.subject.SubjectId
import ch.ergon.studentmarks.domain.model.user.ApprenticeSupervisorPairId
import ch.ergon.studentmarks.domain.model.user.Email
import ch.ergon.studentmarks.domain.model.user.UserId
import ch.ergon.studentmarks.utils.UuidUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class AdminController(
    private val userService: UserService,
    private val subjectService: SubjectService,
    private val semesterService: SemesterService,
    private val uuidUtils: UuidUtils,
) : AdminApi {
    // --- Subjects ---

    override fun adminGetAllSubjects(): ResponseEntity<List<SubjectDto>> =
        ResponseEntity.ok(subjectService.getAllSubjects().map { it.toDto() })

    override fun adminAddSubject(body: SubjectRequestDto): ResponseEntity<SubjectDto> {
        val subject = body.toDomain(SubjectId(uuidUtils.randomUUID()))
        return ResponseEntity.ok(subjectService.addSubject(subject).toDto())
    }

    override fun adminEditSubject(
        subjectId: UUID,
        body: SubjectRequestDto,
    ): ResponseEntity<SubjectDto> = ResponseEntity.ok(subjectService.editSubject(body.toDomain(SubjectId(subjectId))).toDto())

    // --- Semesters ---

    override fun adminGetAllSemesters(
        offset: Int,
        limit: Int,
    ): ResponseEntity<PaginatedSemesterListDto> {
        val paginatedList = semesterService.getAllSemester(PaginationParams(offset, limit))
        return ResponseEntity.ok(paginatedList.toPaginatedSemesterListDto(offset))
    }

    override fun adminAddSemester(body: SemesterRequestDto): ResponseEntity<SemesterDto> =
        ResponseEntity.ok(semesterService.addSemester(body.toDomain(SemesterId(uuidUtils.randomUUID()))).toDto())

    override fun adminEditSemester(
        semesterId: UUID,
        body: SemesterRequestDto,
    ): ResponseEntity<SemesterDto> = ResponseEntity.ok(semesterService.editSemester(body.toDomain(SemesterId(semesterId))).toDto())

    // --- Users ---

    override fun adminReturnAllUsers(searchQuery: String): ResponseEntity<List<UserDto>> =
        ResponseEntity.ok(userService.getAllUser(searchQuery).map { it.toDto() })

    override fun adminAddUser(body: SimpleUserDto): ResponseEntity<Unit> {
        userService.addUser(body.toDomain())
        return ResponseEntity.noContent().build()
    }

    override fun adminEditUser(
        userId: UUID,
        userDto: UserDto,
    ): ResponseEntity<UserDto> = ResponseEntity.ok(userService.editUser(UserId(userId), userDto.toDomain()).toDto())

    override fun adminDeleteUser(userId: UUID): ResponseEntity<UserDto> = ResponseEntity.ok(userService.deleteUser(UserId(userId)).toDto())

    override fun adminRestoreUser(userId: UUID): ResponseEntity<UserDto> =
        ResponseEntity.ok(userService.restoreUser(UserId(userId)).toDto())

    override fun adminGetDeletedUsers(): ResponseEntity<List<UserDto>> = ResponseEntity.ok(userService.getDeletedUsers().map { it.toDto() })

    override fun adminDeleteDeletedUser(userId: UUID): ResponseEntity<UserDto> =
        ResponseEntity.ok(userService.deleteDeletedUser(UserId(userId)).toDto())

    // --- Connections ---

    override fun adminGetConnections(supervisorId: String): ResponseEntity<List<ApprenticeSupervisorPairDto>> {
        val pairs =
            if (supervisorId.isBlank()) {
                userService.getAllConnections()
            } else {
                userService.getApprenticeSupervisorPairs(UserId(supervisorId))
            }
        // For the admin view we need full user details — for now return with IDs only
        // TODO: Enrich with user details via query service when admin view needs it
        return ResponseEntity.ok(
            pairs.map { pair ->
                val apprentice = userService.getUser(pair.apprenticeId)
                val supervisor = userService.getUser(pair.supervisorId)
                ch.ergon.studentmarks.application.query.model
                    .ApprenticeSupervisorPairWithUsers(
                        pair,
                        apprentice,
                        supervisor,
                    ).toDto()
            },
        )
    }

    override fun adminConnect(body: ApprenticeConnectRequestDto): ResponseEntity<ApprenticeSupervisorPairDto> {
        val pair =
            userService.connectApprenticeToSupervisor(
                Email(body.apprenticeEmail),
                Email(body.supervisorEmail),
            )
        val apprentice = userService.getUser(pair.apprenticeId)
        val supervisor = userService.getUser(pair.supervisorId)
        return ResponseEntity.ok(
            ch.ergon.studentmarks.application.query.model
                .ApprenticeSupervisorPairWithUsers(
                    pair,
                    apprentice,
                    supervisor,
                ).toDto(),
        )
    }

    override fun adminUpdateConnections(
        apprenticeSupervisorPairDto: List<ApprenticeSupervisorPairDto>,
    ): ResponseEntity<List<ApprenticeSupervisorPairDto>> {
        apprenticeSupervisorPairDto.forEach {
            userService.updateApprenticeSupervisorConnection(it.toDomain())
        }
        return ResponseEntity.ok(apprenticeSupervisorPairDto)
    }

    override fun adminDeleteConnection(connectionId: UUID): ResponseEntity<ApprenticeSupervisorPairDto> {
        val pair = userService.deleteConnection(ApprenticeSupervisorPairId(connectionId))
        val apprentice = userService.getUser(pair.apprenticeId)
        val supervisor = userService.getUser(pair.supervisorId)
        return ResponseEntity.ok(
            ch.ergon.studentmarks.application.query.model
                .ApprenticeSupervisorPairWithUsers(
                    pair,
                    apprentice,
                    supervisor,
                ).toDto(),
        )
    }

    // --- Search ---

    override fun adminSearchApprentices(searchQuery: String): ResponseEntity<List<SimpleUserDto>> =
        ResponseEntity.ok(userService.getMatchingApprentice(searchQuery).map { it.toDto() })

    override fun adminSearchSupervisors(searchQuery: String): ResponseEntity<List<SimpleUserDto>> =
        ResponseEntity.ok(userService.getMatchingSupervisor(searchQuery).map { it.toDto() })
}
