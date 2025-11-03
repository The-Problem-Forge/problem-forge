package ru.nsu.problem_forge.controller.problem

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.nsu.problem_forge.dto.problem.*
import ru.nsu.problem_forge.service.UserService
import ru.nsu.problem_forge.service.problem.ProblemCheckerService
import ru.nsu.problem_forge.service.problem.ProblemGeneratorService
import ru.nsu.problem_forge.service.problem.ProblemSolutionsService
import ru.nsu.problem_forge.service.problem.ProblemTestsService
import ru.nsu.problem_forge.service.problem.ProblemValidatorService
import ru.nsu.problem_forge.type.problem.FileFormat

@RestController
@RequestMapping("/api/problems/{problemId}")
class ProblemFilesController(
    private val problemSolutionsService: ProblemSolutionsService,
    private val problemCheckerService: ProblemCheckerService,
    private val problemValidatorService: ProblemValidatorService,
    private val problemGeneratorService: ProblemGeneratorService,
    private val problemTestsService: ProblemTestsService,
    private val userService: UserService
) {



    // Checker endpoints
    @GetMapping("/checker")
    fun getChecker(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CheckerResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val checker = problemCheckerService.getChecker(problemId, user.id!!)
        return ResponseEntity.ok(checker)
    }

    @PutMapping("/checker")
    fun setChecker(
        @PathVariable problemId: Long,
        @RequestBody checkerDto: CheckerDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CheckerResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val checker = problemCheckerService.setChecker(problemId, user.id!!, checkerDto)
        return ResponseEntity.ok(checker)
    }

    @DeleteMapping("/checker")
    fun removeChecker(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        problemCheckerService.removeChecker(problemId, user.id!!)
        return ResponseEntity.noContent().build()
    }

    // New checker endpoints for source and tests
    @GetMapping("/checker/source")
    fun getCheckerSource(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CheckerFullResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val checker = problemCheckerService.getCheckerSource(problemId, user.id!!)
        return ResponseEntity.ok(checker)
    }

    @PutMapping("/checker/source")
    fun updateCheckerSource(
        @PathVariable problemId: Long,
        @RequestBody checkerSourceDto: CheckerSourceDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CheckerFullResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val checker = problemCheckerService.updateCheckerSource(problemId, user.id!!, checkerSourceDto)
        return ResponseEntity.ok(checker)
    }

    @PostMapping("/checker/source")
    fun uploadCheckerSource(
        @PathVariable problemId: Long,
        @RequestParam("source") sourceFile: MultipartFile,
        @RequestParam("language") language: String,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CheckerFullResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val source = String(sourceFile.bytes)
        val dto = CheckerSourceDto(source, language)
        val checker = problemCheckerService.updateCheckerSource(problemId, user.id!!, dto)
        return ResponseEntity.ok(checker)
    }

    @GetMapping("/checker/tests")
    fun getCheckerTests(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<CheckerTestResponse>> {
        val user = userService.findUserByHandle(userDetails.username)
        val tests = problemCheckerService.getCheckerTests(problemId, user.id!!)
        return ResponseEntity.ok(tests)
    }

    @PostMapping("/checker/tests")
    fun addCheckerTest(
        @PathVariable problemId: Long,
        @RequestBody testDto: CheckerTestDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CheckerTestResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val test = problemCheckerService.addCheckerTest(problemId, user.id!!, testDto)
        return ResponseEntity.ok(test)
    }

    @PutMapping("/checker/tests/{testId}")
    fun updateCheckerTest(
        @PathVariable problemId: Long,
        @PathVariable testId: Long,
        @RequestBody testDto: CheckerTestDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<CheckerTestResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val test = problemCheckerService.updateCheckerTest(problemId, testId, user.id!!, testDto)
        return ResponseEntity.ok(test)
    }

    @DeleteMapping("/checker/tests/{testId}")
    fun deleteCheckerTest(
        @PathVariable problemId: Long,
        @PathVariable testId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        problemCheckerService.deleteCheckerTest(problemId, testId, user.id!!)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/checker/run")
    fun runCheckerTests(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Map<Long, String>> {
        val user = userService.findUserByHandle(userDetails.username)
        val results = problemCheckerService.runCheckerTests(problemId, user.id!!)
        return ResponseEntity.ok(results)
    }

    // Validator endpoints
    @GetMapping("/validator/source")
    fun getValidatorSource(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ValidatorFullResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val validator = problemValidatorService.getValidatorSource(problemId, user.id!!)
        return ResponseEntity.ok(validator)
    }

    @PutMapping("/validator/source")
    fun updateValidatorSource(
        @PathVariable problemId: Long,
        @RequestBody validatorSourceDto: ValidatorSourceDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ValidatorFullResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val validator = problemValidatorService.updateValidatorSource(problemId, user.id!!, validatorSourceDto)
        return ResponseEntity.ok(validator)
    }

    @PostMapping("/validator/source")
    fun uploadValidatorSource(
        @PathVariable problemId: Long,
        @RequestParam("source") sourceFile: MultipartFile,
        @RequestParam("language") language: String,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ValidatorFullResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val source = String(sourceFile.bytes)
        val dto = ValidatorSourceDto(source, language)
        val validator = problemValidatorService.updateValidatorSource(problemId, user.id!!, dto)
        return ResponseEntity.ok(validator)
    }

    @GetMapping("/validator/tests")
    fun getValidatorTests(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<ValidatorTestResponse>> {
        val user = userService.findUserByHandle(userDetails.username)
        val tests = problemValidatorService.getValidatorTests(problemId, user.id!!)
        return ResponseEntity.ok(tests)
    }

    @PostMapping("/validator/tests")
    fun addValidatorTest(
        @PathVariable problemId: Long,
        @RequestBody testDto: ValidatorTestDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ValidatorTestResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val test = problemValidatorService.addValidatorTest(problemId, user.id!!, testDto)
        return ResponseEntity.ok(test)
    }

    @PutMapping("/validator/tests/{testId}")
    fun updateValidatorTest(
        @PathVariable problemId: Long,
        @PathVariable testId: Long,
        @RequestBody testDto: ValidatorTestDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ValidatorTestResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val test = problemValidatorService.updateValidatorTest(problemId, testId, user.id!!, testDto)
        return ResponseEntity.ok(test)
    }

    @DeleteMapping("/validator/tests/{testId}")
    fun deleteValidatorTest(
        @PathVariable problemId: Long,
        @PathVariable testId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        problemValidatorService.deleteValidatorTest(problemId, testId, user.id!!)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/validator/run")
    fun runValidatorTests(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Map<Long, String>> {
        val user = userService.findUserByHandle(userDetails.username)
        val results = problemValidatorService.runValidatorTests(problemId, user.id!!)
        return ResponseEntity.ok(results)
    }

    // Generator endpoints (NEW)
    @GetMapping("/generators")
    fun getGenerators(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<GeneratorResponse>> {
        val user = userService.findUserByHandle(userDetails.username)
        val generators = problemGeneratorService.getGenerators(problemId, user.id!!)
        return ResponseEntity.ok(generators)
    }

    @PostMapping("/generators")
    fun addGenerator(
        @PathVariable problemId: Long,
        @RequestBody generatorDto: GeneratorDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<GeneratorResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val generator = problemGeneratorService.addGenerator(problemId, user.id!!, generatorDto)
        return ResponseEntity.ok(generator)
    }

    @PutMapping("/generators/{generatorId}")
    fun updateGenerator(
        @PathVariable problemId: Long,
        @PathVariable generatorId: Long,
        @RequestBody generatorDto: GeneratorDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<GeneratorResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val generator = problemGeneratorService.updateGenerator(problemId, generatorId, user.id!!, generatorDto)
        return ResponseEntity.ok(generator)
    }

    @DeleteMapping("/generators/{generatorId}")
    fun deleteGenerator(
        @PathVariable problemId: Long,
        @PathVariable generatorId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        problemGeneratorService.deleteGenerator(problemId, generatorId, user.id!!)
        return ResponseEntity.noContent().build()
    }

    // Test endpoints (NEW)
    @GetMapping("/tests")
    fun getTests(
        @PathVariable problemId: Long,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<TestResponse>> {
        val user = userService.findUserByHandle(userDetails.username)
        val tests = problemTestsService.getTests(problemId, user.id!!)
        return ResponseEntity.ok(tests)
    }

    @PostMapping("/tests")
    fun addTest(
        @PathVariable problemId: Long,
        @RequestBody testDto: TestDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TestResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val test = problemTestsService.addTest(problemId, user.id!!, testDto)
        return ResponseEntity.ok(test)
    }



    @PutMapping("/tests/{testNumber}")
    fun updateTest(
        @PathVariable problemId: Long,
        @PathVariable testNumber: Int,
        @RequestBody testDto: TestDto,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TestResponse> {
        val user = userService.findUserByHandle(userDetails.username)
        val test = problemTestsService.updateTest(problemId, testNumber, user.id!!, testDto)
        return ResponseEntity.ok(test)
    }

    @DeleteMapping("/tests/{testNumber}")
    fun deleteTest(
        @PathVariable problemId: Long,
        @PathVariable testNumber: Int,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Void> {
        val user = userService.findUserByHandle(userDetails.username)
        problemTestsService.deleteTest(problemId, testNumber, user.id!!)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/tests/reorder")
    fun reorderTests(
        @PathVariable problemId: Long,
        @RequestBody newOrder: List<Int>, // List of test numbers in new order
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<List<TestResponse>> {
        val user = userService.findUserByHandle(userDetails.username)
        val tests = problemTestsService.reorderTests(problemId, user.id!!, newOrder)
        return ResponseEntity.ok(tests)
    }

}
