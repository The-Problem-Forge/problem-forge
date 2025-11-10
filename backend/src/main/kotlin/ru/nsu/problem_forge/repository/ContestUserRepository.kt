package ru.nsu.problem_forge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import ru.nsu.problem_forge.entity.ContestUser

interface ContestUserRepository : JpaRepository<ContestUser, Long> {

    fun findByContestIdAndUserId(contestId: Long, userId: Long): ContestUser?

    fun deleteByContestIdAndUserId(contestId: Long, userId: Long)

    @Query("SELECT cu FROM ContestUser cu WHERE cu.contest.id = :contestId")
    fun findAllByContestId(@Param("contestId") contestId: Long): List<ContestUser>

    fun existsByContestIdAndUserId(contestId: Long, userId: Long): Boolean
}