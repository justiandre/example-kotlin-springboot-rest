package tech.justi.example.kotlin.springboot.rest.dto

data class ResultPage<T>(
        var totalRecords: Long? = null,
        var records: List<T>? = null
)