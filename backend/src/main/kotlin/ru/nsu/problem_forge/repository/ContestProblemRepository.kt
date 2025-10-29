package ru.nsu.problem_forge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.nsu.problem_forge.entity.ContestProblem

interface ContestProblemRepository : JpaRepository<ContestProblem, Long> {

    fun findByContestIdAndProblemId(contestId: Long, problemId: Long): ContestProblem?

    fun existsByContestIdAndProblemId(contestId: Long, problemId: Long): Boolean

    fun findAllByContestId(contestId: Long): List<ContestProblem>

    @Query("""
        SELECT COUNT(cp) > 0 
        FROM ContestProblem cp 
        JOIN cp.contest c 
        JOIN c.contestUsers cu 
        WHERE cp.problem.id = :problemId 
        AND cu.user.id = :userId 
        AND c.id != :excludeContestId
    """)
    fun existsByProblemIdAndUserIdExcludingContest(
        @Param("problemId") problemId: Long,
        @Param("userId") userId: Long,
        @Param("excludeContestId") excludeContestId: Long
    ): Boolean
}