package ru.nsu.problem_forge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.nsu.problem_forge.entity.ValidatorTest

@Repository
interface ValidatorTestRepository : JpaRepository<ValidatorTest, Long> {
    fun findByProblemId(problemId: Long): List<ValidatorTest>
    fun findByProblemIdAndId(problemId: Long, id: Long): ValidatorTest?
}