package ru.nsu.problem_forge.type.problem

import com.fasterxml.jackson.annotation.JsonCreator

enum class FileFormat {
    FILE_NOT_FOUND,
    PDF,
    TEXT,
    MARKDOWN,
    CPP_14,
    CPP_17,
    JAVA_17,
    PYTHON,
    JSON,
    XML,
    IMAGE_PNG,
    IMAGE_JPEG,
    ZIP;

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String): FileFormat {
            return try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Unknown FileFormat: $value")
            }
        }
    }
}