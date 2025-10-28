package ru.nsu.problem_forge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.nsu.problem_forge.entity.Problem

@Repository
interface ProblemRepository : JpaRepository<Problem, Long>

//@Repository
//interface ProblemRepository : JpaRepository<Problem, Long> {
//    @Query("""
//        SELECT p FROM Problem p
//        WHERE p.parentProblemId = :parentProblemId
//        AND p.id IN (
//            SELECT pu.problem.id FROM ProblemUser pu
//            WHERE pu.user.id = :userId
//        )
//    """)
//    fun findByParentProblemIdAndUserId(
//        @Param("parentProblemId") parentProblemId: Long,
//        @Param("userId") userId: Long
//    ): Problem?
//}