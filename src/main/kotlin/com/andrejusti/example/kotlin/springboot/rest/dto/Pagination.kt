package com.andrejusti.example.kotlin.springboot.rest.dto

data class Pagination(
        var page: Int? = null,
        var maxRecords: Int? = null
)