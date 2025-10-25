package ru.nsu.problem_forge.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
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
) : UserDetailsService {

    override fun loadUserByUsername(handle: String): UserDetails {
        return userRepository.findByHandle(handle)
            .orElseThrow { UsernameNotFoundException("User not found: $handle") }
    }

    @Transactional
    fun createUser(handle: String, password: String, email: String): User {
        if (userRepository.existsByHandle(handle)) {
            throw IllegalArgumentException("Handle already exists")
        }
        
        val user = User(
            handle = handle,
            hashedPassword = passwordEncoder.encode(password),
            email = email
        )
        
        return userRepository.save(user)
    }

    fun findUserByHandle(handle: String): User {
        return userRepository.findByHandle(handle)
            .orElseThrow { UsernameNotFoundException("User not found: $handle") }
    }
}