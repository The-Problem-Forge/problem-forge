package ru.nsu.problem_forge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.nsu.problem_forge.entity.ContestProblem

@Repository
interface ContestProblemRepository : JpaRepository<ContestProblem, Long> {

    fun findByContestIdAndProblemId(contestId: Long, problemId: Long): ContestProblem?

    fun findByContestId(contestId: Long): List<ContestProblem>

    fun findByProblemId(problemId: Long): List<ContestProblem>
}