package ru.nsu.problem_forge.service.problem

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import ru.nsu.problem_forge.dto.*
import ru.nsu.problem_forge.dto.problem.PreviewStatus
import ru.nsu.problem_forge.dto.problem.TestPreviewResponse
import ru.nsu.problem_forge.dto.problem.TestPreviewStatus
import ru.nsu.problem_forge.entity.File
import ru.nsu.problem_forge.entity.Problem
import ru.nsu.problem_forge.entity.ProblemUser
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.runner.SimpleRunner
import ru.nsu.problem_forge.type.*
import ru.nsu.problem_forge.type.ProblemInfo
import ru.nsu.problem_forge.type.problem.FileFormat
import ru.nsu.problem_forge.type.problem.ProblemSolution
import ru.nsu.problem_forge.type.problem.ProblemTest
import ru.nsu.problem_forge.type.problem.ProblemGenerator
import ru.nsu.problem_forge.type.problem.SolutionType
import ru.nsu.problem_forge.type.problem.TestType
import java.time.LocalDateTime
import java.util.*

@Tag("integration")
class ProblemTestsServiceIntegrationTest {

    private lateinit var problemTestsService: ProblemTestsService
    private lateinit var fileRepository: FileRepository

    private val problemRepository: ProblemRepository = mockk()
    private val problemUserRepository: ProblemUserRepository = mockk()
    private val runner = SimpleRunner()

    private val problemId = 1L
    private val userId = 1L
    private val user = mockk<ProblemUser> {
        every { user } returns mockk { every { id } returns userId }
        every { role } returns Role.EDITOR
    }

    // Your internal state for files
    private val files = mutableMapOf<Long, File>()
    private var nextFileId = 1L

    // Internal state for problems
    private val problems = mutableMapOf<Long, Problem>()
    private var nextProblemId = 1L


    @BeforeEach
    fun setUp() {
        // Create relaxed mocks
        fileRepository = mockk(relaxed = true)

        // Set up file repository methods
        every { fileRepository.findById(any()) } answers {
            val id = firstArg<Long>()
            Optional.ofNullable(files[id])
        }

        every { fileRepository.save(any()) } answers {
            val file = firstArg<File>()
            if (file.id == null || file.id == 0L) {
                file.id = nextFileId++
            }
            files[file.id!!] = file
            file
        }

        every { fileRepository.deleteById(any()) } answers {
            val id = firstArg<Long>()
            files.remove(id)
        }

        every { fileRepository.delete(any()) } answers {
            val file = firstArg<File>()
            files.remove(file.id)
        }

        // Set up problem repository methods
        every { problemRepository.findById(any()) } answers {
            val id = firstArg<Long>()
            Optional.ofNullable(problems[id])
        }

        every { problemRepository.save(any()) } answers {
            val problem = firstArg<Problem>()
            if (problem.id == null || problem.id == 0L) {
                problem.id = nextProblemId++
            }
            problems[problem.id!!] = problem
            problem
        }

        // Set up problem user repository
        every { problemUserRepository.findByProblemIdAndUserId(problemId, userId) } returns user

        problemTestsService = ProblemTestsService(
            problemRepository,
            problemUserRepository,
            fileRepository,
            runner
        )
    }

    // Add helper methods for problems too
    private fun addProblem(problemInfo: ProblemInfo): Long {
        val problem = Problem().apply {
            this.problemInfo = problemInfo
            modifiedAt = LocalDateTime.now()
            createdAt = LocalDateTime.now()
        }
        return problemRepository.save(problem).id!!
    }

    private fun getProblem(id: Long): Problem {
        return problemRepository.findById(id).get()
    }

    private fun updateProblem(id: Long, update: Problem.() -> Unit) {
        val problem = problemRepository.findById(id).get()
        problem.update()
        problemRepository.save(problem)
    }

    private fun addFile(content: String, format: FileFormat = FileFormat.CPP_17): Long {
        val file = File().apply {
            this.content = content.toByteArray()
            this.format = format
            createdAt = LocalDateTime.now()
            modifiedAt = LocalDateTime.now()
        }
        return fileRepository.save(file).id!!
    }

    private fun getFileContent(id: Long): String {
        return String(fileRepository.findById(id).get().content)
    }

    private fun updateFile(id: Long, newContent: String) {
        val file = fileRepository.findById(id).get()
        file.content = newContent.toByteArray()
        file.modifiedAt = LocalDateTime.now()
        fileRepository.save(file)
    }

    @Test
    fun `full integration test with changes`() {
        // Step 1: Create files and problem
        val solutionFileId = addFile(createSolutionSource())
        val generatorFileId = addFile(createGeneratorSource())

        val problem = createProblemWithAllComponents(solutionFileId, generatorFileId)
        val problemId = problemRepository.save(problem).id!! // Save to our internal state

        // Step 2: First preview generation
        println("=== Step 2: First preview generation ===")
        var result = problemTestsService.getTestsPreview(problemId, userId)
        result = waitForPreviewCompletion(result)

        assertEquals(PreviewStatus.COMPLETED, result.status)
        assertEquals(4, result.tests.size)
        result.tests.forEach { test ->
            assertEquals(TestPreviewStatus.COMPLETED, test.status, "Test ${test.testNumber} failed: ${test.message}")
            println("Test ${test.testNumber}: output=${test.output?.trim()}")
        }

        // Step 3: Change generator source
        println("=== Step 3: Changing generator source ===")
        updateFile(generatorFileId, createUpdatedGeneratorSource())

        // Step 4: Second preview - should detect changes
        println("=== Step 4: Second preview after generator change ===")
        result = problemTestsService.getTestsPreview(problemId, userId)
        result = waitForPreviewCompletion(result)
        assertEquals(PreviewStatus.COMPLETED, result.status)

        // Step 5: Change main solution
        println("=== Step 5: Changing main solution ===")
        updateFile(solutionFileId, createUpdatedSolutionSource())

        // Step 6: Third preview - should detect solution change
        println("=== Step 6: Third preview after solution change ===")
        result = problemTestsService.getTestsPreview(problemId, userId)
        result = waitForPreviewCompletion(result)
        assertEquals(PreviewStatus.COMPLETED, result.status)

        println("=== Integration test completed successfully ===")
    }

    private fun createProblemWithAllComponents(solutionFileId: Long, generatorFileId: Long): Problem {
        return Problem().apply {
            id = problemId
            problemInfo = ProblemInfo(
                solutions = listOf(
                    ProblemSolution(
                        solutionId = 1L,
                        author = userId,
                        file = solutionFileId,
                        solutionType = SolutionType.MAIN_AC
                    )
                ),
                tests = listOf(
                    ProblemTest(testType = TestType.RAW, content = "4 25\n15 20 10 5"),
                    ProblemTest(testType = TestType.RAW, content = "8 85\n20 15 15 30 20 50 20 30"),
                    ProblemTest(testType = TestType.GENERATED, content = "array_gen 10 0"),
                    ProblemTest(testType = TestType.GENERATED, content = "array_gen 20 5")
                ),
                generators = listOf(
                    ProblemGenerator(generatorId = 1L, file = generatorFileId, alias = "array_gen")
                )
            )
            modifiedAt = LocalDateTime.now()
            createdAt = LocalDateTime.now()
        }
    }

    private fun createGeneratorSource(): String {
        return """
            #include "testlib.h"
            #include<bits/stdc++.h>
            #include <vector>
            using namespace std;
            
            int main(int argc, char *argv[]) {
                registerGen(argc, argv, 1);
                int n = opt<int>(1);
                int t = opt<int>(2);
                
                vector<int> arr(n);
                int summ = 0;
                
                for(int i = 0; i < n; i++) {
                    arr[i] = rnd.next(1, 10000);
                    summ += arr[i];
                }
                
                int limit = 1000000000;
                int plus = 1000000;
                if (n <= 1000) {
                    limit = 10000000;
                    plus = 100000;
                }
                int S = rnd.wnext(min(limit, summ + plus), t) + 1;
                println(n, S);
                println(arr);
            }
        """.trimIndent()
    }

    private fun createUpdatedGeneratorSource(): String {
        return """
            #include "testlib.h"
            #include<bits/stdc++.h>
            #include <vector>
            using namespace std;
            
            int main(int argc, char *argv[]) {
                registerGen(argc, argv, 1);
                int n = opt<int>(1);
                int t = opt<int>(2);
                
                vector<int> arr(n);
                int summ = 0;
                
                for(int i = 0; i < n; i++) {
                    arr[i] = rnd.next(100, 5000); // CHANGED RANGE
                    summ += arr[i];
                }
                
                int limit = 1000000; // CHANGED LIMIT
                int plus = 50000;
                if (n <= 1000) {
                    limit = 50000;
                    plus = 5000;
                }
                int S = rnd.wnext(min(limit, summ + plus), t) + 1;
                println(n, S);
                println(arr);
            }
        """.trimIndent()
    }

    private fun createSolutionSource(): String {
        return """
            #include<bits/stdc++.h>
            using namespace std;
            
            int main() {
                int n, s;
                cin >> n >> s;
                
                vector<int> a(2 * n);
                for(int i = 0; i < n; i++) {
                    cin >> a[i];
                    a[n + i] = a[i];
                }
                
                int r = 0;
                int ans = 0, summ = 0;
                
                for(int l = 0; l < n; l++) {
                    if (r < l) {
                        r = l;
                        summ = 0;
                    }
                    
                    while (r - l + 1 <= n && summ + a[r] <= s) {
                        summ += a[r];
                        r++;
                    }
                    
                    ans = max(ans, summ);
                    summ -= a[l];
                }
                
                cout << ans << endl;
                return 0;
            }
        """.trimIndent()
    }

    private fun createUpdatedSolutionSource(): String {
        return """
            #include<bits/stdc++.h>
            using namespace std;
            
            int main() {
                int n, s;
                cin >> n >> s;
                
                vector<int> a(n);
                for(int i = 0; i < n; i++) {
                    cin >> a[i];
                }
                
                int max_sum = 0;
                for(int i = 0; i < n; i++) {
                    int current_sum = 0;
                    for(int j = i; j < n; j++) {
                        current_sum += a[j];
                        if(current_sum <= s) {
                            max_sum = max(max_sum, current_sum);
                        } else {
                            break;
                        }
                    }
                }
                
                cout << max_sum << endl;
                return 0;
            }
        """.trimIndent()
    }

    private fun waitForPreviewCompletion(initialResult: TestPreviewResponse): TestPreviewResponse {
        var result = problemTestsService.getTestsPreview(problemId, userId)
        var attempts = 0
        val maxAttempts = 30

        while (result.status == PreviewStatus.IN_PROGRESS && attempts < maxAttempts) {
            Thread.sleep(1000)
            result = problemTestsService.getTestsPreview(problemId, userId)
            attempts++
        }

        if (result.status == PreviewStatus.IN_PROGRESS) {
            throw AssertionError("Preview generation timed out")
        }

        return result
    }
}