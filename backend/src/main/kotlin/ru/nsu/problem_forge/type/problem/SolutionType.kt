package ru.nsu.problem_forge.type.problem

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.annotation.JsonCreator

enum class SolutionType(@JsonValue val value: String = "", val color: String = "RED") {
    MAIN_AC("Main correct solution", "GREEN"),
    AC("Correct", "GREEN"),
    INCORRECT("Incorrect"),
    TLE("Time limit exceeded"),
    TLE_OR_AC("Time limit exceeded or correct"),
    TLE_OR_ML("Time limit exceeded or Memory limit exceeded"),
    WA("Wrong answer"),
    PE("Presentation error"),
    ML("Memory limit exceeded"),
    FAILED("Failed"),
    DO_NOT_RUN("Do not run");

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String): SolutionType {
            return values().find { it.value == value }
                ?: values().find { it.name == value }
                ?: throw IllegalArgumentException("Unknown SolutionType: $value")
        }
    }

    override fun toString(): String = value
}
