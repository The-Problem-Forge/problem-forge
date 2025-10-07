package ru.nsu.problem_forge.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import ru.nsu.problem_forge.security.JwtUtil
import ru.nsu.problem_forge.service.UserService

@Component
class JwtRequestFilter(
    private val jwtUtil: JwtUtil,
    private val userService: UserService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtRequestFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        logger.debug("Processing authentication for: ${request.requestURI}")

        try {
            val jwt = getJwtFromRequest(request)
            
            if (jwt != null) {
                val username = extractUsernameSafely(jwt)
                
                if (username != null && SecurityContextHolder.getContext().authentication == null) {
                    val userDetails = userService.loadUserByUsername(username)
                    
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        val authentication = UsernamePasswordAuthenticationToken(
                            userDetails, 
                            null, 
                            userDetails.authorities
                        )
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication
                        
                        logger.debug("Authenticated user: $username")
                    } else {
                        logger.debug("Token validation failed for user: $username")
                    }
                }
            }
        } catch (ex: Exception) {
            logger.error("Could not set user authentication in security context", ex)
            // Не прерываем цепочку фильтров - пусть запрос продолжается без аутентификации
        }
        
        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }

    private fun extractUsernameSafely(token: String): String? {
        return try {
            jwtUtil.extractUsername(token)
        } catch (ex: Exception) {
            logger.debug("Failed to extract username from token: ${ex.message}")
            null
        }
    }
}