package ru.nsu.problem_forge.convert

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import ru.nsu.problem_forge.type.ProblemInfo

@Converter
class ProblemInfoConverter : AttributeConverter<ProblemInfo, String> {

    private val objectMapper = ObjectMapper()

    override fun convertToDatabaseColumn(attribute: ProblemInfo): String {
        return objectMapper.writeValueAsString(attribute)
    }

    override fun convertToEntityAttribute(dbData: String): ProblemInfo {
        return objectMapper.readValue(dbData)
    }
}