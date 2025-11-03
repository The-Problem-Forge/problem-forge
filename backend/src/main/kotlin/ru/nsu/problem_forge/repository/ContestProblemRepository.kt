package ru.nsu.problem_forge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.nsu.problem_forge.entity.ContestProblem

@Repository
interface ContestProblemRepository : JpaRepository<ContestProblem, Long> {

    fun findByContestIdAndProblemId(contestId: Long, problemId: Long): ContestProblem?

    fun findByContestId(contestId: Long): List<ContestProblem>

    fun findByProblemId(problemId: Long): List<ContestProblem>

    fun existsByContestIdAndProblemId(contestId: Long, problemId: Long): Boolean

    @Query("SELECT COUNT(cp) > 0 FROM ContestProblem cp JOIN ContestUser cu ON cp.contest.id = cu.contest.id WHERE cp.problem.id = :problemId AND cu.user.id = :userId AND cp.contest.id != :contestId")
    fun existsByProblemIdAndUserIdExcludingContest(@Param("problemId") problemId: Long, @Param("userId") userId: Long, @Param("contestId") contestId: Long): Boolean
}