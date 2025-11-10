package ru.nsu.problem_forge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import ru.nsu.problem_forge.entity.Contest

@Repository
interface ContestRepository : JpaRepository<Contest, Long> {

    @Query("""
        SELECT c FROM Contest c
        JOIN c.contestUsers cu
        WHERE cu.user.id = :userId
    """)
    fun findContestsByUserId(@Param("userId") userId: Long): List<Contest>

    @Query("""
        SELECT cu.role FROM ContestUser cu
        WHERE cu.contest.id = :contestId AND cu.user.id = :userId
    """)
    fun findUserRoleInContest(@Param("contestId") contestId: Long, @Param("userId") userId: Long): String?

    @Query("""
        SELECT cp FROM ContestProblem cp
        JOIN FETCH cp.problem p
        WHERE cp.contest.id = :contestId
        ORDER BY cp.orderIndex
    """)
    fun findContestProblemsByContestId(@Param("contestId") contestId: Long): List<ru.nsu.problem_forge.entity.ContestProblem>
}