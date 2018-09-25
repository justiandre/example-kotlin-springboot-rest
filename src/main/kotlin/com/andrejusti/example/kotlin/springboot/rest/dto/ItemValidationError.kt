package com.andrejusti.example.kotlin.springboot.rest.dto

open class ItemValidationError(
        var errorLocation: String,
        var errorType: String,
        var message: String? = null,
        var context: List<Any>? = null
)


