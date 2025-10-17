Title: Frontend-Only Implementation Guide for Problem Forge

Objective
- Implement ONLY the frontend features described in [requirement.md](requirement.md) using the backend contract defined in [backend_api.md](backend_api.md). Do not change or add any backend code or endpoints.
- Where an endpoint is unavailable or returns 404/501, the UI must degrade gracefully (disable actions, show clear messages) without attempting to implement backend workarounds.

Scope boundaries (strict)
- No backend changes. Treat [backend_api.md](backend_api.md) as the authoritative contract.
- If /api/auth/me or any other endpoint is not present yet, the frontend must:
  - Fall back to token presence for initial auth guard.
  - Handle 401 responses by clearing auth state and redirecting to Home.
  - Keep UI elements visible but disabled or show informative banners where the feature depends on missing endpoints.

Global Constraints (must follow)
- Frontend (React/JS)
  - Run npx prettier frontend --write after EVERY change (see [AGENTS.md](AGENTS.md)).
  - JSDoc is mandatory for ALL functions, classes, and interfaces. Include @param, @return, @throws where applicable.
  - Styling must be in separate CSS files (avoid inline styles; migrate any existing inline styles).
  - ES6 imports; order imports: React, third-party, then local.
  - Error handling: try-catch in async functions; console.error a user-readable message (avoid logging sensitive data).
  - Library approvals (approved by owner):
    - react-router-dom (routing)
    - MathJax v3 via script tag injection with a thin React wrapper
    - monaco-editor (code editor)
    - react-beautiful-dnd (drag-and-drop for reorder)
  - Do not change files that the user changed before invocations. Prefer adding new files and minimal integrations; ask before touching [frontend/src/App.js](frontend/src/App.js) and other existing files if large refactors are needed.

Security and privacy (frontend responsibilities)
- Never log tokens or source code contents.
- Validate inputs client-side where helpful, but always defer final authority to server responses.
- For file uploads and large text fields, display sizes and warn near limits to prevent unexpected failures.

What You Will Build (frontend-only)
- Routing shell with AuthGuard, public and private routes.
- Pages:
  - Home (public + authenticated variants)
  - Login/Register
  - Contests list and Contest detail
  - Task Editor with tabs: General, Statement, Checker, Validator, Tests, Solutions, Invocations
- UI integrations with backend via service modules that strictly follow [backend_api.md](backend_api.md).
- MathJax-based preview for LaTeX fields.
- Download flows for LaTeX/PDF and solution files.
- Reorder UX using approved react-beautiful-dnd, with Up/Down fallback logic if desired.

Frontend-Only Phase Plan (execute in order)

Phase 0: Setup and Guards
- Add react-router-dom and define routes:
  - / (Home)
  - /login (Login/Register)
  - /contests (My contests)
  - /contests/:contestId (Contest detail)
  - /contests/:contestId/tasks/:taskId/* (Task editor parent with nested tabs)
- Implement AuthGuard:
  - On app mount, attempt authAPI.me(); if 200, hydrate UI; on 401, clear token+login and redirect to Home.
  - If /me is not implemented, use token presence as a soft auth indicator and rely on 401 handling on API calls.
- Add a top-level layout (nav bar + logout) with separate CSS.

Phase 1: Auth and Home
- Login/Register page:
  - Use authAPI.login/register; on success store token+login, navigate to /.
  - Migrate existing inline styles in [frontend/src/components/Login.js](frontend/src/components/Login.js) to a CSS file; add JSDoc to component and handlers.
- Home:
  - Public: marketing landing with buttons to /login.
  - Authenticated: list contests via contestsAPI.list(); show “New Contest” modal (contestsAPI.create()).

Phase 2: Contest detail and Tasks (basic)
- Contest detail header: contestsAPI.get(contestId).
- Tasks list: tasksAPI.listByContest(contestId).
- Add task modal: tasksAPI.create(contestId, { title, description? }).
- Reorder tasks:
  - Implement with react-beautiful-dnd (approved). OnDragEnd compute new orderIndex list and call contestsAPI.reorderTasks(contestId, order).
  - Provide keyboard accessible Up/Down buttons as a fallback or progressive enhancement.
  - On failure, rollback optimistically updated UI and show error.

Phase 3: Task Editor tabs (General and Statement)
- General tab:
  - GET/PUT generalAPI.get(taskId)/update(taskId, payload).
  - Fields: name, inFile, outFile, timeLimitMs, memoryLimitMb with basic validation and helper text.
- Statement tab:
  - GET/PUT statementAPI.get/update with textareas: statementTex, inputFormatTex, outputFormatTex, notesTex, tutorialTex.
  - MathJax v3 preview toggle: load via script tag; create a small Wrapper component to re-typeset on content changes.
  - Exports:
    - LaTeX export: statementAPI.exportTex(taskId) with responseType: "blob"; infer filename from Content-Disposition.
    - PDF export: statementAPI.exportPdf(taskId); if 501, show non-blocking info banner indicating LaTeX export is available.

Phase 4: Checker and Validator
- Source management:
  - Upload source with multipart POST; show language autodetect with manual override.
  - View/edit source with GET (content included) and PUT to save; use monaco-editor for editing (approved).
- Tests CRUD:
  - Checker tests: manage inputText, participantOutputText, referenceOutputText, expectedVerdict (OK, WRONG_ANSWER, PRESENTATION_ERROR, CRASHED).
  - Validator tests: manage inputText, expectedVerdict (VALID, INVALID).
- Run tests:
  - POST run endpoints; render per-test results inline (verdict, timeMs, memoryKb, stdoutPreview/stderrPreview).
  - Handle long outputs by truncating in UI if needed; provide “show more” expanders client-side only.

Phase 5: Problem Tests and Generators
- Problem tests:
  - List and CRUD: manual text and file upload variants.
  - When uploading a file, show filename and size; read-only preview.
- Generators:
  - Upload/list/view/edit source; run generator with args and optional count; append generated tests to list with provenance labels.

Phase 6: Solutions
- List solutions with name, language, compilerVariant (for cpp), type.
- Actions:
  - Compile check: show status and stdout/stderr previews.
  - View/edit source (monaco-editor); PUT updates.
  - Download source; use responseType: "blob" and Content-Disposition filename.

Phase 7: Invocations
- Create invocation by selecting subsets of solutions/tests; show solution type in the selector.
- Results matrix:
  - Sticky headers; rows=solutions, columns=tests.
  - Per cell: verdict icon + time/memory.
  - Summary panel per solution: passed count, max time/memory.

Phase 8: Polish and Hardening
- Move all inline styles to CSS.
- JSDoc coverage check (components, hooks, services).
- Prettier run: npx prettier frontend --write.
- UX for edge cases: empty states, permission errors (403), unauthenticated (401), large content warnings.

Services (API client layer)
- Extend [frontend/src/services/api.js](frontend/src/services/api.js) with domain-specific modules:
  - authAPI: login(login, password), register(login, password), me()
  - contestsAPI: list(), get(id), create({ name, description? }), update(id, payload), delete(id), reorderTasks(contestId, order[])
  - tasksAPI: listByContest(contestId), get(taskId), update(taskId, payload), delete(taskId), create(contestId, payload)
  - generalAPI: get(taskId), update(taskId, payload)
  - statementAPI: get(taskId), update(taskId, payload), exportTex(taskId), exportPdf(taskId)
  - checkerAPI: uploadSource(taskId, file, language?), getSource(taskId), updateSource(taskId, content, language?), listTests(taskId), createTest(taskId, payload), updateTest(taskId, testId, payload), deleteTest(taskId, testId), runTests(taskId, testIds?)
  - validatorAPI: same surface pattern as checkerAPI with its test shapes
  - testsAPI: list(taskId), createText(taskId, payload), createFile(taskId, file, description?), update(taskId, testId, payload), delete(taskId, testId)
  - generatorsAPI: list(taskId), uploadSource(taskId, file, language?, notes?), getSource(taskId, generatorId), updateSource(taskId, generatorId, content, language?, notes?), run(taskId, generatorId, { args?, count? })
  - solutionsAPI: list(taskId), uploadSource(taskId, file, language?, compilerVariant?, name?, type?), getSource(taskId, solutionId), update(taskId, solutionId, payload), download(taskId, solutionId), compile(taskId, solutionId)
  - invocationsAPI: create(taskId, payload), list(taskId), get(taskId, invocationId), matrix(taskId, invocationId)
- For file downloads and exports, set responseType: "blob" and parse Content-Disposition to suggest filenames.
- Add JSDoc for all methods. Include @throws {Error} with a brief message for known failure cases.

Routing map (React Router v6)
- / (Home)
  - If unauthenticated: landing
  - If authenticated: contests list
- /login (Login/Register)
- /contests
- /contests/:contestId
- /contests/:contestId/tasks/:taskId (Task Editor)
  - index: General
  - statement
  - checker
  - validator
  - tests
  - solutions
  - invocations

Validation and Error UX
- Client-side validation for required fields and positive integers.
- Friendly error banners on API failures; details panel for advanced diagnostics when available (stderrPreview).
- 401 handling: clear token+login, redirect to Home.
- 403 handling: display permission error; disable mutating actions.

Edge Cases and Non-intuitive Details
- Reorder race conditions: optimistic UI with rollback; always refresh list after a successful reorder call.
- Missing endpoints (e.g., /me, PDF export): keep UI visible but show disabled controls or an info banner; do not hide features entirely.
- Large LaTeX/source content: show length counters and warn near limits (1 MB).
- Download UX: provide meaningful filenames and mime types; always handle failures (show toast/banner).
- Monaco editor performance: load the editor lazily (code-splitting) to avoid increasing initial bundle size.

Testing (manual, frontend-only)
- npm start in frontend; validate:
  - Auth: register/login; token persists; guard redirects on 401s.
  - Contests: list/create; error handling when server rejects.
  - Tasks: add and reorder; reload retains server order.
  - Task tabs:
    - General/Statement CRUD.
    - LaTeX preview; exports (LaTeX ok, PDF may 501 with graceful message).
    - Checker/Validator: upload/edit source, CRUD tests, run tests (render results).
    - Tests and Generators flows.
    - Solutions: list/compile/view-edit/download.
    - Invocations: create/list/matrix and summaries.

Acceptance Criteria (frontend behavior per requirement.md)
- Home: public info vs authenticated list + create contest.
- Login/Register flows; guards redirect unauthenticated users to Home.
- Contest page: tasks list, add by name, reorder persisted.
- Task tabs:
  - General: fields load/save.
  - Statement: LaTeX preview; export LaTeX; PDF shows 501 notice when not available.
  - Checker/Validator: upload/edit source, tests CRUD, run tests with result display.
  - Tests: list/add manual/file/generator; generators upload/edit/run.
  - Solutions: list, compile, edit, download.
  - Invocations: create with subsets; matrix shows verdict/time/memory; per-solution summaries.

Commands Reference (frontend-only)
- Development: npm start (from frontend directory)
- Production build: npm run build (from frontend directory)
- Formatting: npx prettier frontend --write

Finalize
- Do not change backend code. Use [backend_api.md](backend_api.md) as the contract.
- Keep [tasks.md](tasks.md) as the high-level plan and milestones.
- Ensure UI remains informative and non-breaking when a backend feature is unavailable (e.g., 501 for PDF export).
