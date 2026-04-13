# Code Review: feature/openapi-spec-driven-api

Reviewed 2026-04-13 — 208 files changed, ~4600 additions, ~2500 deletions.

Status legend: `[ ]` todo, `[x]` done, `[-]` won't do

---

## Bugs

- [x] **1. `JooqMarkRepository.update()` silently drops semester changes**
  `infrastructure/persistence/repository/JooqMarkRepository.kt:74-91`
  The `update()` method is missing `.set(MARK.SEMESTER_ID, mark.semesterId.value)`. The `save()` method has it, `update()` doesn't. A user can change a mark's semester, get 200 OK, and nothing changes in the DB.

- [x] **2. `findRelevantForUser` subquery includes soft-deleted marks**
  `infrastructure/persistence/repository/JooqSemesterRepository.kt:73-80`
  The subquery selecting semester IDs is missing `.andNot(MARK.DELETED)`. Every other mark query filters deleted marks — this one doesn't.

- [x] **3. `adminUpdateConnections` returns stale data**
  `api/admin/AdminController.kt:138-145`
  The old controller returned fresh data from the DB after update. The new one echoes back the input DTOs unchanged, not confirming the update actually persisted.

- [-] **4. `deleteExpiredSoftDeleted` lacks `DELETED = true` guard**
  `infrastructure/persistence/repository/JooqMarkRepository.kt:144-149`
  Only checks `DELETED_TIME < olderThan`. Mitigated by NULL semantics in practice, but a missing safety net.

---

## Security Regressions

- [x] **5. Missing `@PreAuthorize` on `MarkService.calculate()`**
  `application/service/MarkService.kt:88`
  Old version enforced `IS_CURRENT_USER or IS_SUPERVISOR_OF or ROLE_ADMIN`. Annotation was dropped, not moved elsewhere. Any authenticated user can call calculate with any userId.

- [x] **6. Missing `@PreAuthorize` on `UserService.updateApprenticeSupervisorConnection()`**
  `application/service/UserService.kt:93`
  Same pattern — old `@PreAuthorize` was dropped. The endpoint lives under `/api/admin/connections` which looks admin-only, but there's no actual authorization enforcement at any layer.

---

## Migration Risk

- [-] **7. Modified existing Flyway migrations**
  `V0.1.1`, `V0.2.2`, `V0.3.2`, `V0.3.3`
  Four existing migration files were edited for DDLDatabase compatibility. Flyway checksums every migration — if these were ever applied to a real database, Flyway will refuse to start. If only ever run locally with Testcontainers, this is fine.

---

## Test Coverage Regressions

- [x] **8. Email test assertions gutted**
  `test/.../api/EmailTest.kt`
  Old tests had 19 verify calls checking recipient list, email body HTML (date, subject, mark, weight, value). New tests only check the email subject line string. Body and recipients are `anyString()` / `anyList()`.

- [x] **9. `edit_user_boxi` self-assertion (pre-existing)**
  `test/.../api/UserApiTest.kt:394`
  `assertThat(editedUser, is(editedUser))` compares to itself, always passes. Should be `is(editedUserBoxi)`. Pre-existing on main, but worth fixing.

---

## Architecture

- [x] **10. Query services bypass the interface pattern**
  `application/query/AgendaQueryService.kt`, `SemesterOverviewQueryService.kt`
  Both inject concrete `Jooq*` classes instead of interfaces. Breaks testability and the layering established everywhere else.

- [x] **11. Exception classes live in `infrastructure` but are used by `application`**
  `infrastructure/security/exeptions/` used by `application/service/UserService.kt`
  Domain-semantic exceptions (`DeleteOwnUserException`, `MarkNotFoundException`, etc.) belong in `domain`. Also: `exeptions` is a typo for `exceptions`.

- [x] **12. `MarkController` directly injects `SubjectRepository`**
  `api/student/MarkController.kt:30`
  Controller calls `subjectRepository.findById()` to enrich responses. This orchestration belongs in `MarkService`.

- [x] **13. `SemesterRequest` schema missing `format: date`**
  `openapi/schemas/semester-request.yaml`
  `startDate` and `endDate` are `type: string` with no `format: date`, generating `String` instead of `LocalDate`. Forces manual `LocalDate.parse()` with no error handling. Other schemas (`Semester`, `Mark`) correctly use `format: date`.
