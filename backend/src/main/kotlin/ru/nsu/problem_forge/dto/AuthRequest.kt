package ru.nsu.problem_forge.dto

data class AuthRequest(
    val login: String, // changed from username
    val password: String
)