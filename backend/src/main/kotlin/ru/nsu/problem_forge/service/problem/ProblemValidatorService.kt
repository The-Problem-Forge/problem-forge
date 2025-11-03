package ru.nsu.problem_forge.service.problem

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.problem.*
import ru.nsu.problem_forge.entity.File
import ru.nsu.problem_forge.entity.ValidatorTest
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.repository.ValidatorTestRepository
import ru.nsu.problem_forge.runner.Runner
import ru.nsu.problem_forge.type.Role
import java.time.LocalDateTime

@Service
class ProblemValidatorService(
    private val problemRepository: ProblemRepository,
    private val problemUserRepository: ProblemUserRepository,
    private val fileRepository: FileRepository,
    private val validatorTestRepository: ValidatorTestRepository,
    private val runner: Runner
) {

    fun getValidatorSource(problemId: Long, userId: Long): ValidatorFullResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        val validatorId = problem.problemInfo.validator
        val source = if (validatorId != null) {
            val file = fileRepository.findById(validatorId)
                .orElseThrow { IllegalArgumentException("Validator file not found") }
            String(file.content)
        } else ""

        val language = problem.problemInfo.validatorLanguage ?: "cpp"

        val tests = validatorTestRepository.findByProblemId(problemId).map {
            ValidatorTestResponse(it.id!!, it.input, it.verdict)
        }

        // For now, runResults is empty; could be cached or computed on demand
        val runResults = emptyMap<Long, String>()

        return ValidatorFullResponse(source, language, tests, runResults)
    }

    @Transactional
    fun updateValidatorSource(problemId: Long, userId: Long, dto: ValidatorSourceDto): ValidatorFullResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        var fileId = problem.problemInfo.validator
        var language = problem.problemInfo.validatorLanguage ?: "cpp"

        // Update source if provided
        if (dto.source != null) {
            val file = if (fileId != null) {
                val existingFile = fileRepository.findById(fileId)
                    .orElseThrow { IllegalArgumentException("Validator file not found") }

                existingFile.apply {
                    content = dto.source.toByteArray()
                    modifiedAt = LocalDateTime.now()
                }
            } else {
                File().apply {
                    format = ru.nsu.problem_forge.type.problem.FileFormat.CPP_17 // Default, but could be determined from language
                    content = dto.source.toByteArray()
                    createdAt = LocalDateTime.now()
                    modifiedAt = LocalDateTime.now()
                }
            }

            val savedFile = fileRepository.save(file)
            fileId = savedFile.id
        }

        // Update language if provided
        if (dto.language != null) {
            language = dto.language
        }

        // Update problem info
        val updatedProblemInfo = problem.problemInfo.copy(
            validator = fileId,
            validatorLanguage = language
        )
        problem.problemInfo = updatedProblemInfo
        problem.modifiedAt = LocalDateTime.now()

        problemRepository.save(problem)

        // Return full response
        return getValidatorSource(problemId, userId)
    }

    fun getValidatorTests(problemId: Long, userId: Long): List<ValidatorTestResponse> {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        return validatorTestRepository.findByProblemId(problemId).map {
            ValidatorTestResponse(it.id!!, it.input, it.verdict)
        }
    }

    @Transactional
    fun addValidatorTest(problemId: Long, userId: Long, dto: ValidatorTestDto): ValidatorTestResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        val test = ValidatorTest(
            problemId = problemId,
            input = dto.input,
            verdict = dto.verdict
        )

        val savedTest = validatorTestRepository.save(test)

        return ValidatorTestResponse(
            savedTest.id!!,
            savedTest.input,
            savedTest.verdict
        )
    }

    @Transactional
    fun updateValidatorTest(problemId: Long, testId: Long, userId: Long, dto: ValidatorTestDto): ValidatorTestResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        val test = validatorTestRepository.findByProblemIdAndId(problemId, testId)
            ?: throw IllegalArgumentException("Test not found")

        val updatedTest = test.copy(
            input = dto.input,
            verdict = dto.verdict,
            modifiedAt = LocalDateTime.now()
        )

        val savedTest = validatorTestRepository.save(updatedTest)

        return ValidatorTestResponse(
            savedTest.id!!,
            savedTest.input,
            savedTest.verdict
        )
    }

    @Transactional
    fun deleteValidatorTest(problemId: Long, testId: Long, userId: Long) {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        val test = validatorTestRepository.findByProblemIdAndId(problemId, testId)
            ?: throw IllegalArgumentException("Test not found")

        validatorTestRepository.delete(test)
    }

    fun runValidatorTests(problemId: Long, userId: Long): Map<Long, String> {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        val validatorId = problem.problemInfo.validator
            ?: throw IllegalArgumentException("No validator configured")

        val file = fileRepository.findById(validatorId)
            .orElseThrow { IllegalArgumentException("Validator file not found") }

        val validatorSource = String(file.content)
        val language = problem.problemInfo.validatorLanguage ?: "cpp"

        val tests = validatorTestRepository.findByProblemId(problemId)

        val results = mutableMapOf<Long, String>()

        for (test in tests) {
            try {
                // Run validator with test input and check if it matches expected verdict
                val actualVerdict = runner.runValidatorTest(validatorSource, language, test.input)
                val expectedVerdict = test.verdict
                val result = if (actualVerdict == expectedVerdict) "OK" else "WA"
                results[test.id!!] = result
            } catch (e: Exception) {
                results[test.id!!] = "CRASHED"
            }
        }

        return results
    }
}