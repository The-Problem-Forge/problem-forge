package ru.nsu.problem_forge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.nsu.problem_forge.entity.ProblemUser

@Repository
interface ProblemUserRepository : JpaRepository<ProblemUser, Long> {
    
    fun findByUserId(userId: Long): List<ProblemUser>
    
    fun findByProblemId(problemId: Long): List<ProblemUser>
    
    fun findByProblemIdAndUserId(problemId: Long, userId: Long): ProblemUser?
}