# Problem Forge Frontend-Only Implementation Plan

Scope correction
- This document is updated to reflect a frontend-only implementation scope. Do not implement or modify backend code.
- Treat the backend interface documented in [backend_api.md](backend_api.md) as the stable contract. The frontend must consume those APIs and handle unavailable endpoints gracefully (e.g., show meaningful errors or disabled actions).
- Any backend data model and execution notes are for context only so the UI can be designed correctly; they must not be implemented here.

References to current code for context:
- Project guardrails: [AGENTS.md](AGENTS.md)
- Existing frontend entry points: [frontend/src/App.js](frontend/src/App.js), [frontend/src/components/Login.js](frontend/src/components/Login.js), [frontend/src/components/TaskManager.js](frontend/src/components/TaskManager.js), [frontend/src/services/api.js](frontend/src/services/api.js)
- Backend API contract for consumption only: [backend_api.md](backend_api.md)

Style and constraints from AGENTS.md (enforced)
- Run npx prettier frontend --write after every change
- All functions/components must include JSDoc
- Styling in separate CSS files (move inline styles out)
- Ask before adding libraries
- Do not modify files the user changed before unless asked explicitly


1. Scope summary from requirement.md (frontend features to deliver)

- Home
  - Logged out: show basic product info + buttons to Login/Register
  - Logged in: show list of contests from GET /api/contests, allow New Contest (POST /api/contests)
  - Clicking a contest opens its page
- Login
  - Separate login/register page with basic auth (POST /api/auth/login, POST /api/auth/register)
  - Redirect unauthenticated users to Home from protected pages
- Contest
  - Show contest name/description (GET /api/contests/{contestId}); list tasks (GET /api/contests/{contestId}/tasks)
  - Allow adding a task by name (POST /api/contests/{contestId}/tasks)
  - Allow reordering tasks (PUT /api/contests/{contestId}/tasks/reorder) without relying on index positions
- Task editor with tabs
  - General (GET/PUT /api/tasks/{taskId}/general): name, in/out filenames, time limit (ms), memory (MB)
  - Statement (GET/PUT /api/tasks/{taskId}/statement): LaTeX fields with MathJax preview; export LaTeX/PDF (GET export endpoints)
  - Checker: upload/view/edit source; manage tests; run tests and show results (checker endpoints)
  - Validator: similar to checker but different test model and verdicts (validator endpoints)
  - Tests: list/add via manual text, file upload, or generator run; upload/view/edit generators (tests/generators endpoints)
  - Solutions: list, compile check, view/edit, download (solutions endpoints)
  - Invocations: select subsets to run; show results matrix with time/memory + summaries (invocations endpoints)


2. Frontend architecture and key decisions

- Routing and guards
  - Use React Router v6 for pages and nested routes for task tabs
  - Public routes: /, /login
  - Private routes: /contests, /contests/:contestId, /contests/:contestId/tasks/:taskId/*, etc.
  - AuthGuard reads token from localStorage. On initial mount, call GET /api/auth/me for hydration; if the endpoint is unavailable (404/501) or fails, fall back to token presence as a soft auth check and keep handling 401s globally
- State and data fetching
  - Local component state per screen; fetch data on mount or route changes
  - API layer organized by domain under frontend/src/services with JSDoc for each method
- UI style
  - Clean, readable, responsive layout
  - Consistent spacing, monospace blocks for code/LaTeX, clear primary/secondary buttons
  - Separate CSS files per page/component (e.g., src/styles/*.css)
- Task reorder
  - Initial: Up/Down buttons to avoid extra dependencies
  - Optional enhancement (needs approval): drag-and-drop via react-beautiful-dnd
- Editors
  - Initial: textareas for code and LaTeX
  - Optional enhancement (needs approval): Monaco editor for richer experience
- Tables/matrices
  - Use simple HTML tables with sticky headers for invocation matrix. Each cell shows verdict + time/memory
- Error handling
  - Try/catch around async calls; inline user-friendly error banners
  - Global 401 handling: clear storage and navigate to home


3. UX flows and details per page

3.1 Home
- Logged out: title + short description; two primary buttons to /login; maybe minimal features overview
- Logged in: "My Contests" with cards/list using GET /api/contests; "New Contest" opens modal with name (required) and description (optional)

3.2 Login/Register
- Single page with toggle for login vs register
- On success: save token + login in localStorage; navigate to "/"
- Show validation errors inline

3.3 Contest page
- Header: contest name/description from GET /api/contests/{contestId}
- Tasks list from GET /api/contests/{contestId}/tasks
  - New Task modal: title (required), description (optional) via POST
  - Reorder via Up/Down; on change, call PUT /api/contests/{contestId}/tasks/reorder with the entire order array
  - Clicking task navigates to task editor default tab

3.4 Task editor tabs (routes under /contests/:contestId/tasks/:taskId)
- General: Form bound to GET/PUT /api/tasks/{taskId}/general
- Statement:
  - Textareas: statementTex, inputFormatTex, outputFormatTex, notesTex, tutorialTex
  - Preview toggle renders with MathJax (v3) via script tag wrapper
  - Export LaTeX (GET /statement/export/tex) and PDF (GET /statement/export/pdf). If PDF returns 501, show a helpful notice
- Checker:
  - Upload source (multipart POST), show language autodetect with manual override
  - View/edit source (GET with content, PUT to save)
  - Checker tests CRUD and run; render results inline per test row
- Validator:
  - Mirrored UI with its test model and verdicts
- Tests:
  - List tests; add via manual text (JSON), upload file (multipart), or run generator; show generator provenance
  - Generators: upload/list/view/edit, with optional run producing tests
- Solutions:
  - List solutions (name, language, compiler variant for cpp, type)
  - Compile check action; view/edit content; download endpoint for file
- Invocations:
  - Create panel: choose solutionIds + testIds
  - Results matrix with sticky headers; show cell verdict, time/memory; side summary per solution (passed count, max time/memory)


4. Services (API client) structure and contracts

Create typed service wrappers under frontend/src/services with methods closely mapping to [backend_api.md](backend_api.md). Include JSDoc and error handling. Example modules:
- authAPI: login, register, me
- contestsAPI: list, get, create, update, delete, reorderTasks
- tasksAPI: listByContest, get, update, delete, create
- generalAPI: get, update (for /tasks/{taskId}/general)
- statementAPI: get, update, exportTex, exportPdf
- checkerAPI: uploadSource, getSource, updateSource; tests CRUD; runTests
- validatorAPI: uploadSource, getSource, updateSource; tests CRUD; runTests
- testsAPI: list, create (manual or multipart), update, delete
- generatorsAPI: list, uploadSource, getSource, updateSource, run
- solutionsAPI: list, uploadSource, getSource, update, download, compile
- invocationsAPI: create, list, get, matrix

Notes:
- Keep axios interceptor to attach JWT
- For endpoints returning files (export/download), use responseType: "blob" and suggest filenames


5. Routing map (React Router v6)

- / (Home)
  - Logged out state: marketing landing
  - Logged in state: My Contests
- /login (Login/Register)
- /contests (My contests)
- /contests/:contestId (Contest detail)
- /contests/:contestId/tasks/:taskId (Task editor parent)
  - default: General tab
  - /statement
  - /checker
  - /validator
  - /tests
  - /solutions
  - /invocations


6. Implementation phases (frontend-only)

Phase 1: Project scaffolding and guard
- Add React Router v6, AuthGuard, and top-level layout (nav + logout)
- Implement /login page; wire authAPI.login/register; store token+login; global 401 handler
- On app mount, try authAPI.me to hydrate; fallback to token presence if unavailable

Phase 2: Home and Contests
- Home page (public vs authenticated states)
- Contests list (GET /api/contests)
- New Contest modal (POST /api/contests)

Phase 3: Contest page and Tasks basics
- Contest header (GET /api/contests/{contestId})
- Tasks list (GET); add task modal (POST)
- Reorder via Up/Down + PUT reorder

Phase 4: Task General and Statement
- General tab UI (GET/PUT)
- Statement tab with MathJax preview (GET/PUT)
- Export LaTeX and PDF actions (GET export endpoints, handle 501 for PDF)

Phase 5: Checker and Validator
- Source upload/view/edit
- Tests CRUD and Run tests with inline results
- Robust error display for compile/run failures

Phase 6: Tests and Generators
- Problem tests list and creation via manual/form-data/generator
- Generators upload/list/view/edit/run

Phase 7: Solutions
- List solutions; compile check; view/edit; download

Phase 8: Invocations
- Create invocation; list and view matrix with summaries
- Loading states and sticky headers

Phase 9: Polish and hardening
- Move any remaining inline styles to CSS
- JSDoc coverage check
- Prettier formatting run
- Empty states, permission errors, and input validation UX

Each phase ships small, testable UI increments bound to backend_api.md endpoints


7. Acceptance criteria mapped to requirement.md (frontend behavior)

- Home
  - Logged out shows info and Login/Register
  - Logged in shows contests; can create a contest
  - Clicking a contest navigates to its page
- Login/Register
  - Works end-to-end; protected routes redirect to Home when unauthenticated
- Contest
  - Shows name/description and tasks
  - Can add a task by name
  - Can reorder tasks; order persists after refresh
- Task - General
  - Fields load/save correctly
- Task - Statement
  - LaTeX fields render in preview; Export LaTeX downloads .tex
  - Export PDF attempts download; user sees clear message if 501 Not Implemented
- Checker
  - Upload/edit source
  - Manage tests; run tests; see results per test
- Validator
  - Same as checker with VALID/INVALID verdicts
- Tests
  - List tests; add via manual/file/generator; manage generators
- Solutions
  - List; compile check; view/edit; download
- Invocations
  - Create run with subsets; matrix renders with per-cell verdict/time/memory and per-solution summary


8. Risks and mitigations (frontend)

- Backend endpoints not yet available
  - Mitigation: Feature-gate UI buttons (disabled with tooltip), show clear 501/404/400 error banners, and log a concise console.error
- Large payloads (LaTeX and sources)
  - Mitigation: Use textareas with length indicators; warn on near-limit inputs
- Reorder race conditions
  - Mitigation: Optimistic UI with rollback if server rejects; always refresh list after successful PUT
- PDF export unavailable
  - Mitigation: Show a non-blocking info banner guiding users to use LaTeX export
- Editor UX
  - Mitigation: Start with textareas; plan optional Monaco after approval


9. Developer notes and conventions (frontend-only)

- Always add JSDoc for functions/components and service methods
- Prefer CSS modules or per-component CSS files under src/styles
- Keep services small and cohesive, each matching backend_api.md endpoints
- Centralize API error handling utilities (e.g., map axios errors to user-friendly strings)
- After implementing or changing UI, run:
  - npx prettier frontend --write


10. Minimal library usage and approvals

- Required: react-router-dom
- MathJax: prefer v3 via script tag with a thin React wrapper component
- Optional after approval: monaco-editor, react-beautiful-dnd
- Do not add additional libraries without explicit approval


11. Open integration assumptions

- The contract in [backend_api.md](backend_api.md) is available in the environment; if /api/auth/me is not implemented, the guard should fall back to presence of token but still expect 401s on protected API calls and react accordingly.
- File downloads are handled with responseType: "blob" and Content-Disposition awareness for inferred filenames
- For multipart uploads, server expects "file" field for the source; additional fields per endpoint are documented in [backend_api.md](backend_api.md)