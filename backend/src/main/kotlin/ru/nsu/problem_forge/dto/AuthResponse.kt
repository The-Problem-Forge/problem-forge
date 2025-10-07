package ru.nsu.problem_forge.dto

data class AuthResponse(
    val token: String,
    val login: String // changed from username
)