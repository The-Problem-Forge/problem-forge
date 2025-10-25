package ru.nsu.problem_forge.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.nsu.problem_forge.entity.User
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByHandle(handle: String): Optional<User>
    fun existsByHandle(handle: String): Boolean
    fun findByEmail(email: String): Optional<User>
}