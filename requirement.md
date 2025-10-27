# Frontend
## Home
1. If user is not logged in, home page should contain basic information about the project
2. If user is logged in, home page should display all contests that this user has access to
3. Clicking on the contest should open a page with that contest
4. User should be able to add a new contest (only name and description needed for creation)

## Login
1. There should be a separate login/register page with basic auth. After loggin
2. If user is not logged in, they should be redirected to the home page from any page

## Contest
1. Contest page should display basic information about this contest (name/description) and a list of tasks
2. Clicking on the task should open a page of that task
3. User should be able to add a new task by name
4. User should be able to reorder tasks. (Therefore, tasks should not be referred by index in the list internally)

## Task
1. Task editing page should contain multiple tabs (sub-pages)
### General
On this page user should be able to edit:
1. Name
2. Input/output filenames
3. Time limit (in ms)
4. Memory limit (in MB)

### Statement
On this page user should be able to edit:
1. Problem statement in LaTeX
2. Input format in LaTeX
3. Output format in LaTeX
4. Notes in LaTeX
5. Tutorial in LaTeX
All LaTeX input fields should have an ability to render math formulas (MathJax) and a preview
Additionaly, user should be able to export final LaTeX and PDF of the problem (see example.tex and example.pdf)

### Checker
On this page user should be able:
1. Upload a source file of the program that will check solutions (checker).
And select language of the program or autodetect it. 
2. View source code of the uploaded checker and be able to edit it.
3. Add tests for the checker
    1. Each test should contain text input, participant output, reference output and an expected verdict
    2. Verdict can be one of: OK, WRONG_ANSWER, PRESENTATION_ERROR, CRASHED
4. There should be a button to run the tests
5. Test run results should display next to the test in the UI

### Validator
On this page user should be able:
1. Upload a source file of the program that will validate input data (validator).
And select language of the program or autodetect it. 
2. View source code of the uploaded checker and be able to edit it.
3. Add tests for the checker
    1. Each test should contain text input and an expected verdict
    2. Verdict can be one of: VALID, INVALID
4. There should be a button to run the tests
5. Test run results should display next to the test in the UI

### Tests
Tests page should display all tests for the problem.
1. Preview of the input data or a generator statement
2. Description field
Users should be able to:
1. Add new tests by typing in the input data, uploading input file or running a generator command on the server.
2. Upload new generators (scripts that can generate new tests), select language of the program or autodetect it.
3. View source code of the uploaded generators and be able to edit it.

### Solutions
This page should display a list of already uploaded solutions (source code files). Each solution should have:
1. Name (by default derived from filename)
2. Language with a specific compiler if it's c++
3. Type. One of: 
    - Main correct solution
    - Correct
    - Incorrect
    - Time limit exceeded
    - Time limit exceeded or correct
    - Time limit exceeded or memory limit exceeded
    - Wrong answer
    - Presentation error
    - Memory limit exceeded
    - Do not run
    - Failed
User should be able to:
1. Check solutions for successful compilation (try to compile on the server)
2. View source code of the uploaded solutions and be able to edit it.
3. Be able to download a solution file

## Invocations
This page should display information about invocations:
1. test - solution table that in each cell shows a result of the solution for this test. Add time/memory information of the run to the cell as well as 
2. Little summary for each solution: number of passed tests, max time/memory used
User should be able to:
1. Create a new invocation be selecting a subset of solutions and tests to run
2. When selecting solutions, their type should be visible