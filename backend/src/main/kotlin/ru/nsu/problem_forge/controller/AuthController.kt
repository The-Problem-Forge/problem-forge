package ru.nsu.problem_forge.controller

import ru.nsu.problem_forge.dto.AuthRequest
import ru.nsu.problem_forge.dto.AuthResponse
import ru.nsu.problem_forge.security.JwtUtil
import ru.nsu.problem_forge.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userService: UserService,
    private val jwtUtil: JwtUtil
) {

    @PostMapping("/login")
    fun login(@RequestBody authRequest: AuthRequest): ResponseEntity<AuthResponse> {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(authRequest.login, authRequest.password)
        )
        
        val user = userService.findUserByHandle(authRequest.login)
        val token = jwtUtil.generateToken(user.handle)
        
        return ResponseEntity.ok(AuthResponse(token, user.handle))
    }

    @PostMapping("/register")
    fun register(@RequestBody authRequest: AuthRequest): ResponseEntity<AuthResponse> {
        val user = userService.createUser(
            handle = authRequest.login,
            password = authRequest.password,
            email = "${authRequest.login}@example.com"
        )
        
        val token = jwtUtil.generateToken(user.handle)
        return ResponseEntity.ok(AuthResponse(token, user.handle))
    }
}