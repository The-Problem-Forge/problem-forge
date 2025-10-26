package ru.nsu.problem_forge.type.problem

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.annotation.JsonCreator

enum class TestType {
    RAW,
    GENERATED;

    @JsonValue
    fun getValue(): String {
        return this.name
    }

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String): TestType {
            return valueOf(value)
        }
    }
}