package ru.nsu.problem_forge.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.core.type.TypeReference

@Converter
class ListLongConverter : AttributeConverter<List<Long>, String> {
    private val mapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<Long>?): String? {
        return attribute?.let { mapper.writeValueAsString(it) }
    }

    override fun convertToEntityAttribute(dbData: String?): List<Long>? {
        return dbData?.let { mapper.readValue(it, object : TypeReference<List<Long>>() {}) }
    }
}

@Converter
class InvocationResultsConverter : AttributeConverter<Map<Long, List<InvocationTestResult>>, String> {
    private val mapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: Map<Long, List<InvocationTestResult>>?): String? {
        return attribute?.let { mapper.writeValueAsString(it) }
    }

    override fun convertToEntityAttribute(dbData: String?): Map<Long, List<InvocationTestResult>>? {
        return dbData?.let { 
            mapper.readValue(it, object : TypeReference<Map<Long, List<InvocationTestResult>>>() {}) 
        }
    }
}