package ru.nsu.problem_forge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.nsu.problem_forge.entity.Invocation
import ru.nsu.problem_forge.entity.InvocationStatus

@Repository
interface InvocationRepository : JpaRepository<Invocation, Long> {
    
    fun findByProblemIdOrderByCreatedAtDesc(problemId: Long): List<Invocation>
    
    fun findByProblemIdAndUserIdOrderByCreatedAtDesc(problemId: Long, userId: Long): List<Invocation>
    
    fun findByProblemIdAndStatusOrderByCreatedAtDesc(problemId: Long, status: InvocationStatus): List<Invocation>
    
    @Query("SELECT i FROM Invocation i WHERE i.problemId = :problemId AND i.status IN :statuses ORDER BY i.createdAt DESC")
    fun findByProblemIdAndStatusInOrderByCreatedAtDesc(
        @Param("problemId") problemId: Long, 
        @Param("statuses") statuses: List<InvocationStatus>
    ): List<Invocation>
    
    fun findFirstByProblemIdOrderByCreatedAtDesc(problemId: Long): Invocation?
}