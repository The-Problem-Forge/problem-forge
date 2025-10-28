package ru.nsu.problem_forge.service.nsuts

import org.springframework.stereotype.Service
import ru.nsu.problem_forge.service.problem.ProblemPackageService

@Service
class NsutsPackageCoordinator(
    private val problemPackageService: ProblemPackageService,
    private val nsutsUploadService: NsutsUploadService
) {

//    fun createAndUploadPackage(problemId: Long, userId: Long): String {
//        val packageData = problemPackageService.generatePackageSync(problemId, userId)
//
//        // Here we can do not have package... change logic
//        return nsutsUploadService.uploadProblemPackage(packageData, problemId, 2000, 512, listOf(
//            """
//
//            """.trimIndent()
//        ))
//    }
}