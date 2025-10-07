package ru.nsu.problem_forge.service

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nsu.problem_forge.entity.User
import ru.nsu.problem_forge.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun loadUserByUsername(login: String): User {
        return userRepository.findByLogin(login)
            .orElseThrow { UsernameNotFoundException("User not found: $login") }
    }

    @Transactional
    fun createUser(login: String, password: String, email: String): User {
        if (userRepository.existsByLogin(login)) {
            throw IllegalArgumentException("Login already exists")
        }
        
        val user = User(
            login = login,
            hashedPassword = passwordEncoder.encode(password),
            email = email
        )
        
        return userRepository.save(user)
    }
}