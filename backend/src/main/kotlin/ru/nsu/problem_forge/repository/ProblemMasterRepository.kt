package ru.nsu.problem_forge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.nsu.problem_forge.entity.ProblemMaster

@Repository
interface ProblemMasterRepository : JpaRepository<ProblemMaster, Long>