package ru.nsu.problem_forge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.nsu.problem_forge.entity.CheckerTest

@Repository
interface CheckerTestRepository : JpaRepository<CheckerTest, Long> {
    fun findByProblemId(problemId: Long): List<CheckerTest>
    fun findByProblemIdAndId(problemId: Long, id: Long): CheckerTest?
}