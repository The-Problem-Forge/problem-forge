package ru.nsu.problem_forge.convert

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import ru.nsu.problem_forge.type.Changelog

@Converter
class ChangelogConverter : AttributeConverter<Changelog, String> {

    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: Changelog): String {
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): Changelog {
        return objectMapper.readValue(dbData)
    }
}