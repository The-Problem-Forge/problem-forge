package ru.nsu.problem_forge.service.problem


import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.dto.problem.GeneratorDto
import ru.nsu.problem_forge.dto.problem.GeneratorResponse
import ru.nsu.problem_forge.entity.File
import ru.nsu.problem_forge.repository.FileRepository
import ru.nsu.problem_forge.repository.ProblemRepository
import ru.nsu.problem_forge.repository.ProblemUserRepository
import ru.nsu.problem_forge.type.Role
import ru.nsu.problem_forge.type.problem.ProblemGenerator
import java.time.LocalDateTime
import java.util.*

@Service
class ProblemGeneratorService(
    private val problemRepository: ProblemRepository,
    private val problemUserRepository: ProblemUserRepository,
    private val fileRepository: FileRepository
) {

    fun getGenerators(problemId: Long, userId: Long): List<GeneratorResponse> {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        return problem.problemInfo.generators.map { generator ->
            val file = fileRepository.findById(generator.file)
                .orElseThrow { IllegalArgumentException("File not found for generator ${generator.generatorId}") }

            GeneratorResponse(
                generatorId = generator.generatorId,
                file = Base64.getEncoder().encodeToString(file.content),
                format = file.format,
                alias = generator.alias,
                createdAt = file.createdAt,
                modifiedAt = file.modifiedAt
            )
        }
    }

    @Transactional
    fun addGenerator(problemId: Long, userId: Long, generatorDto: GeneratorDto): GeneratorResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        // Create file entity
        val file = File().apply {
            format = generatorDto.format
            content = Base64.getDecoder().decode(generatorDto.file)
            createdAt = LocalDateTime.now()
            modifiedAt = LocalDateTime.now()
        }
        val savedFile = fileRepository.save(file)

        // Generate generator ID
        val generatorId = System.currentTimeMillis()

        // Create generator
        val generator = ProblemGenerator(
            generatorId = generatorId,
            file = savedFile.id!!,
            alias = generatorDto.alias
        )

        // Update problem info
        val currentGenerators = problem.problemInfo.generators.toMutableList()
        currentGenerators.add(generator)

        val updatedProblemInfo = problem.problemInfo.copy(generators = currentGenerators)
        problem.problemInfo = updatedProblemInfo
        problem.modifiedAt = LocalDateTime.now()

        problemRepository.save(problem)

        return GeneratorResponse(
            generatorId = generatorId,
            file = generatorDto.file,
            format = generatorDto.format,
            alias = generator.alias,
            createdAt = savedFile.createdAt,
            modifiedAt = savedFile.modifiedAt
        )
    }

    @Transactional
    fun updateGenerator(problemId: Long, generatorId: Long, userId: Long, generatorDto: GeneratorDto): GeneratorResponse {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        // Find existing generator
        val currentGenerators = problem.problemInfo.generators.toMutableList()
        val generatorIndex = currentGenerators.indexOfFirst { it.generatorId == generatorId }
        if (generatorIndex == -1) {
            throw IllegalArgumentException("Generator not found")
        }

        val oldGenerator = currentGenerators[generatorIndex]

        // Update file
        val file = fileRepository.findById(oldGenerator.file)
            .orElseThrow { IllegalArgumentException("File not found for generator $generatorId") }

        file.apply {
            format = generatorDto.format
            content = Base64.getDecoder().decode(generatorDto.file)
            modifiedAt = LocalDateTime.now()
        }
        val updatedFile = fileRepository.save(file)

        // Update generator
        val updatedGenerator = oldGenerator.copy(
            alias = generatorDto.alias
        )
        currentGenerators[generatorIndex] = updatedGenerator

        // Update problem info
        val updatedProblemInfo = problem.problemInfo.copy(generators = currentGenerators)
        problem.problemInfo = updatedProblemInfo
        problem.modifiedAt = LocalDateTime.now()

        problemRepository.save(problem)

        return GeneratorResponse(
            generatorId = generatorId,
            file = generatorDto.file,
            format = generatorDto.format,
            alias = updatedGenerator.alias,
            createdAt = updatedFile.createdAt,
            modifiedAt = updatedFile.modifiedAt
        )
    }

    @Transactional
    fun deleteGenerator(problemId: Long, generatorId: Long, userId: Long) {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { IllegalArgumentException("Problem not found") }

        val problemUser = problemUserRepository.findByProblemIdAndUserId(problemId, userId)
            ?: throw SecurityException("No access to problem")

        if (problemUser.role < Role.EDITOR) {
            throw SecurityException("Editor role required")
        }

        // Find generator
        val currentGenerators = problem.problemInfo.generators.toMutableList()
        val generator = currentGenerators.find { it.generatorId == generatorId }
            ?: throw IllegalArgumentException("Generator not found")

        // Remove generator
        currentGenerators.removeIf { it.generatorId == generatorId }

        // Update problem info
        val updatedProblemInfo = problem.problemInfo.copy(generators = currentGenerators)
        problem.problemInfo = updatedProblemInfo
        problem.modifiedAt = LocalDateTime.now()

        problemRepository.save(problem)

        // Delete file
        fileRepository.deleteById(generator.file)
    }
}