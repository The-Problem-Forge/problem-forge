# Backend API Specification

Purpose
- Define all endpoints required to implement requirement.md, aligned with current backend under /api and JWT auth in [backend/src/main/kotlin/ru/nsu/problem_forge/config/SecurityConfig.kt](backend/src/main/kotlin/ru/nsu/problem_forge/config/SecurityConfig.kt).
- Clarify request/response schemas, status codes, auth, file handling, and rationale so implementation and frontend can proceed deterministically.

Scope (frontend-only consumption)
- This document is a contract for the frontend. In this task, do NOT implement or modify backend services. The UI must consume these endpoints and handle missing or unavailable ones gracefully (e.g., disable actions, show informative messages, and handle 401/403/501 appropriately).

Base
- Base URL: /api
- Auth: JWT Bearer in Authorization header (already wired by interceptor in [frontend/src/services/api.js](frontend/src/services/api.js))
- Content types:
  - application/json for JSON bodies
  - multipart/form-data for uploads (source files)
  - application/octet-stream or text/plain for raw downloads
- Errors: JSON error body
  - { error: string, message: string, code: string, details?: any }

Security and roles
- All endpoints except /auth/* require authentication.
- Contest-level role-based access control:
  - Owner: full control including member management
  - Editor: can create/update/delete tasks and assets, cannot manage members
  - Viewer: can read
- If membership is not implemented in Milestone 1, scope all contest lists by owner only and forbid cross-user access.

IDs and ordering
- All IDs are opaque server-generated.
- Task reordering uses explicit orderIndex persisted in DB; no implicit ordering by ID allowed.

Languages and compilers
- Language enum: cpp, python, java (extendable)
- For C++ compilerVariant enum: gnu, clang (optional)
- Autodetect by filename extension and optional shebang; manual override allowed.

Verdicts
- Checker test expected verdict set: OK, WRONG_ANSWER, PRESENTATION_ERROR, CRASHED
- Validator test expected verdict set: VALID, INVALID
- Invocation result verdict set: OK, WRONG_ANSWER, PRESENTATION_ERROR, CRASHED, TIME_LIMIT_EXCEEDED, MEMORY_LIMIT_EXCEEDED, RUNTIME_ERROR, COMPILE_ERROR

Limits and safety
- Upload max size configurable, default 1 MB per source file
- Text fields trimmed and size-limited (e.g., 1 MB for statement sections)
- Exec runs initially synchronous with strict timeout, then move to async worker later


1. Auth

1.1 POST /api/auth/login
- Body: { login: string, password: string }
- 200: { token: string, login: string }
- 401 if invalid credentials

1.2 POST /api/auth/register
- Body: { login: string, password: string }
- 200: { token: string, login: string }
- 409 if login exists
- Rationale: simple onboarding

1.3 GET /api/auth/me
- Auth required
- 200: { login: string, email: string, roles: string[] }
- Rationale: hydrate client on load, verify token freshness


2. Contests

2.1 GET /api/contests
- Auth required
- Query (optional): page, size (future)
- 200: Array of contests user can access
  - [{ id, name, description, role: Owner|Editor|Viewer, createdAt, updatedAt }]
- Rationale: home page when logged in

2.2 POST /api/contests
- Auth required
- Body: { name: string, description?: string }
- 201: { id, name, description, role: Owner, createdAt, updatedAt }
- 400 if missing name
- Rationale: create new contest (requirement: only name and description)

2.3 GET /api/contests/{contestId}
- Auth required
- 200: { id, name, description, role, createdAt, updatedAt }
- 403 if no access
- Rationale: contest header

2.4 PUT /api/contests/{contestId}
- Auth required, Owner or Editor
- Body: { name?: string, description?: string }
- 200: updated contest
- Rationale: edit name/description

2.5 DELETE /api/contests/{contestId}
- Auth required, Owner
- 204 on success
- Rationale: cleanup; not strictly in requirements but standard

2.6 Members management (optional if ACL not in initial milestone)
- GET /api/contests/{contestId}/members
  - 200: [{ userId, login, role }]
- POST /api/contests/{contestId}/members
  - Body: { login: string, role: Owner|Editor|Viewer } (Owner only; assigning Owner means transferring ownership)
  - 201: created member
- PUT /api/contests/{contestId}/members/{userId}
  - Body: { role }
  - 200: updated
- DELETE /api/contests/{contestId}/members/{userId}
  - 204


3. Tasks

3.1 GET /api/contests/{contestId}/tasks
- Auth required (Viewer+)
- 200: [{ id, title, description, orderIndex, createdAt, updatedAt }]

3.2 POST /api/contests/{contestId}/tasks
- Auth required (Editor+)
- Body: { title: string, description?: string }
- 201: { id, title, description, orderIndex }
- Rationale: add task by name

3.3 GET /api/tasks/{taskId}
- Auth required (Viewer+ for the contest)
- 200: { id, contestId, title, description, orderIndex }

3.4 PUT /api/tasks/{taskId}
- Auth required (Editor+)
- Body: { title?: string, description?: string }
- 200: updated task

3.5 DELETE /api/tasks/{taskId}
- Auth required (Editor+)
- 204

3.6 PUT /api/contests/{contestId}/tasks/reorder
- Auth required (Editor+)
- Body: { order: [{ taskId: number, orderIndex: number }, ...] }
- 200: [{ id, title, description, orderIndex }]
- Notes:
  - Operation must be atomic; validate all taskIds belong to the contest; ensure contiguous or allow sparsity but sort ascending client-side.
- Rationale: tasks can be reordered and not referred by index implicitly


4. Task General

4.1 GET /api/tasks/{taskId}/general
- Auth required (Viewer+)
- 200: { name, inFile, outFile, timeLimitMs, memoryLimitMb }
  - name defaults to task title if unset

4.2 PUT /api/tasks/{taskId}/general
- Auth required (Editor+)
- Body: { name?: string, inFile?: string, outFile?: string, timeLimitMs?: number, memoryLimitMb?: number }
- 200: updated object
- Validation: timeLimitMs positive int, memoryLimitMb positive int
- Rationale: General tab fields


5. Task Statement

5.1 GET /api/tasks/{taskId}/statement
- Auth required (Viewer+)
- 200: { statementTex, inputFormatTex, outputFormatTex, notesTex, tutorialTex }

5.2 PUT /api/tasks/{taskId}/statement
- Auth required (Editor+)
- Body: { statementTex?: string, inputFormatTex?: string, outputFormatTex?: string, notesTex?: string, tutorialTex?: string }
- 200: updated

5.3 GET /api/tasks/{taskId}/statement/export/tex
- Auth required (Viewer+)
- 200: text/plain with Content-Disposition: attachment; filename="task_{taskId}.tex"
- Rationale: allow download of LaTeX

5.4 GET /api/tasks/{taskId}/statement/export/pdf
- Auth required (Viewer+)
- 200: application/pdf with Content-Disposition: attachment; filename="task_{taskId}.pdf"
- 501 if PDF generation not available
- Rationale: export PDF per requirement


6. Checker

6.1 POST /api/tasks/{taskId}/checker/source
- Auth required (Editor+)
- Content-Type: multipart/form-data
  - Fields:
    - file: source file
    - language?: string enum (optional, override autodetect)
- 201: { language, filename, size, updatedAt }
- Validation: only text source allowed; size limit

6.2 GET /api/tasks/{taskId}/checker/source
- Auth required (Viewer+)
- 200: { language, filename, size, updatedAt, content: string }
- Note: Include content due to UI requirement to view/edit

6.3 PUT /api/tasks/{taskId}/checker/source
- Auth required (Editor+)
- Body: { content: string, language?: string }
- 200: { language, filename, size, updatedAt }
- Rationale: edit in-place

6.4 CRUD for checker tests
- GET /api/tasks/{taskId}/checker/tests
  - 200: [{ id, inputText, participantOutputText, referenceOutputText, expectedVerdict }]
- POST /api/tasks/{taskId}/checker/tests
  - Body: { inputText: string, participantOutputText: string, referenceOutputText: string, expectedVerdict: enum }
  - 201: created test object
- PUT /api/tasks/{taskId}/checker/tests/{testId}
  - Body: fields as above (partial)
  - 200: updated
- DELETE /api/tasks/{taskId}/checker/tests/{testId}
  - 204

6.5 POST /api/tasks/{taskId}/checker/tests/run
- Auth required (Editor+)
- Body:
  - { testIds?: number[] } (if omitted, run all)
- 200: results array by test id
  - [{ testId, verdict, timeMs, memoryKb, stdoutPreview, stderrPreview }]
- Rationale: button to run tests with results near each test


7. Validator

7.1 POST /api/tasks/{taskId}/validator/source
- Same semantics as checker/source

7.2 GET /api/tasks/{taskId}/validator/source
- Same semantics

7.3 PUT /api/tasks/{taskId}/validator/source
- Same semantics

7.4 CRUD for validator tests
- GET /api/tasks/{taskId}/validator/tests
  - 200: [{ id, inputText, expectedVerdict }]
- POST /api/tasks/{taskId}/validator/tests
  - Body: { inputText: string, expectedVerdict: enum }
  - 201
- PUT /api/tasks/{taskId}/validator/tests/{testId}
  - 200
- DELETE /api/tasks/{taskId}/validator/tests/{testId}
  - 204

7.5 POST /api/tasks/{taskId}/validator/tests/run
- Body: { testIds?: number[] }
- 200: [{ testId, verdict, timeMs, memoryKb, stdoutPreview, stderrPreview }]


8. Problem Tests (for solutions/invocations)

8.1 GET /api/tasks/{taskId}/tests
- Auth required (Viewer+)
- 200: [{ id, description, inputPreview: string, generatorId?: number }]

8.2 POST /api/tasks/{taskId}/tests
- Auth required (Editor+)
- Body option A (manual text):
  - { description?: string, inputText: string }
- Body option B (file upload): multipart/form-data with file, description?
- 201: created test with id

8.3 PUT /api/tasks/{taskId}/tests/{testId}
- Body: { description?: string, inputText?: string }
- 200

8.4 DELETE /api/tasks/{taskId}/tests/{testId}
- 204


9. Generators

9.1 POST /api/tasks/{taskId}/generators/source
- Auth required (Editor+)
- multipart/form-data
  - file: source
  - language?: enum
  - notes?: string
- 201: { id, language, filename, size, notes, updatedAt }

9.2 GET /api/tasks/{taskId}/generators
- Auth required (Viewer+)
- 200: [{ id, language, filename, size, notes, updatedAt }]

9.3 GET /api/tasks/{taskId}/generators/{generatorId}/source
- 200: { id, language, filename, size, notes, content }

9.4 PUT /api/tasks/{taskId}/generators/{generatorId}/source
- Body: { content: string, language?: enum, notes?: string }
- 200

9.5 POST /api/tasks/{taskId}/generators/{generatorId}/run
- Auth required (Editor+)
- Body: { args?: string[], count?: number } (optional count to generate multiple tests)
- 200: { generated: [{ testId, description, inputPreview }], logs?: string }
- Rationale: create tests by running a generator on server


10. Solutions

10.1 POST /api/tasks/{taskId}/solutions/source
- Auth required (Editor+)
- multipart/form-data
  - file: source
  - language?: enum
  - compilerVariant?: enum (for cpp)
  - name?: string
  - type?: enum
- 201: { id, name, language, compilerVariant?, type, filename, size, updatedAt }

10.2 GET /api/tasks/{taskId}/solutions
- Auth required (Viewer+)
- 200: [{ id, name, language, compilerVariant?, type, compileStatus?, updatedAt }]

10.3 GET /api/tasks/{taskId}/solutions/{solutionId}/source
- 200: { id, name, language, compilerVariant?, type, filename, size, content, updatedAt }

10.4 PUT /api/tasks/{taskId}/solutions/{solutionId}
- Body: { name?: string, language?: enum, compilerVariant?: enum, type?: enum, content?: string }
- 200: updated
- Notes: allow editing source in place

10.5 GET /api/tasks/{taskId}/solutions/{solutionId}/download
- 200: application/octet-stream with Content-Disposition: attachment; filename="{name or filename}"

10.6 POST /api/tasks/{taskId}/solutions/{solutionId}/compile
- 200: { status: "OK"|"ERROR", stdoutPreview, stderrPreview, compiledAt }
- 400 if invalid language or source missing
- Rationale: compile check only


11. Invocations

11.1 POST /api/tasks/{taskId}/invocations
- Auth required (Editor+ or Viewer? For now Editor+)
- Body: { name?: string, solutionIds: number[], testIds: number[] }
- 201: { id, taskId, name, status: "queued"|"running"|"finished"|"failed", createdAt }
- Rationale: create a run selecting subsets

11.2 GET /api/tasks/{taskId}/invocations
- Auth required (Viewer+)
- 200: [{ id, name, status, createdAt, finishedAt? }]

11.3 GET /api/tasks/{taskId}/invocations/{invocationId}
- 200: { id, name, status, createdAt, startedAt?, finishedAt?, summary: [{ solutionId, passed, maxTimeMs, maxMemoryKb }] }

11.4 GET /api/tasks/{taskId}/invocations/{invocationId}/matrix
- 200: {
    solutions: [{ id, name, language, compilerVariant?, type }],
    tests: [{ id, description }],
    results: [{ solutionId, testId, verdict, timeMs, memoryKb }]
  }
- Rationale: render matrix and per-solution summary on UI

11.5 POST /api/tasks/{taskId}/invocations/{invocationId}/cancel (optional for async worker)
- 202: { status: "cancelling" }
- 409 if not cancellable


12. Files and downloads

Already covered under solutions download and statement exports. If generic file download is added for diagnostics, ensure it requires Editor+.


13. Status codes and errors

- 200 OK for successful GET/PUT/POST where applicable
- 201 Created for successful resource creation
- 204 No Content for successful delete
- 400 Bad Request for validation errors (include details)
- 401 Unauthorized if token missing/invalid
- 403 Forbidden for insufficient role or non-member
- 404 Not Found for missing or not accessible IDs (avoid leaking existence)
- 409 Conflict for duplicates or illegal state transition
- 413 Payload Too Large for uploads beyond limit
- 415 Unsupported Media Type for uploads with wrong content type
- 429 Too Many Requests for rate limits (future)
- 500 Internal Server Error for unhandled cases
- 501 Not Implemented for PDF export if not available yet

Error shape standard:
- { error: "ValidationError", message: "Field timeLimitMs must be positive", code: "VALIDATION_ERROR", details: { field: "timeLimitMs" } }


14. Validation rules summary

- Contest: name required, 1..200 chars
- Task: title required, 1..200 chars
- General: timeLimitMs and memoryLimitMb positive integers
- Statement fields: limit to e.g., 1 MB each
- Uploads: only text/* or language-appropriate extensions; reject binaries
- Solutions: type must be one of requirement list
- Reorder: each task in contest appears once; transactionally update


15. Rationale mapping to requirement.md

- Home and contests require GET/POST contests
- Login/register covered by AuthController plus new me endpoint
- Contest page needs task list, add, reorder and edit
- Task General and Statement with preview and export need General and Statement CRUD + export endpoints
- Checker/Validator upload/edit/test-run flows covered by source endpoints, test CRUD, run endpoints
- Tests and Generators covered by problem tests CRUD and generator upload/edit/run
- Solutions list, compile check, edit, and download covered
- Invocations matrix, summary and creation covered by invocation endpoints


16. Implementation notes (server)

- Keep controller packages grouped: auth, contests, tasks, statement, checker, validator, tests, generators, solutions, invocations
- Use @PreAuthorize or method-level checks using contest membership/ownership resolved by taskId or contestId
- Store source files on disk in a data volume: data/contests/{contestId}/tasks/{taskId}/{assetType}/{uuid}.ext
- Keep DB records referencing current active file path and checksum
- For synchronous runs, guard with timeouts and capture stdout/stderr up to configured length