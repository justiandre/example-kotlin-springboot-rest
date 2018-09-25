package tech.justi.example.kotlin.springboot.rest.exception

import tech.justi.example.kotlin.springboot.rest.dto.ItemValidationError

data class ValidationException(val itemValidationErrors: List<ItemValidationError>) : RuntimeException()