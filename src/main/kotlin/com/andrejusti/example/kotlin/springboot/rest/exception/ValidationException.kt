package com.andrejusti.example.kotlin.springboot.rest.exception

import com.andrejusti.example.kotlin.springboot.rest.dto.ItemValidationError

data class ValidationException(val itemValidationErrors: List<ItemValidationError>) : RuntimeException()