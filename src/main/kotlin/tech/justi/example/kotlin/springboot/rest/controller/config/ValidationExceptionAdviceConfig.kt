package tech.justi.example.kotlin.springboot.rest.controller.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import tech.justi.example.kotlin.springboot.rest.dto.ItemValidationError
import tech.justi.example.kotlin.springboot.rest.exception.ValidationException


@ControllerAdvice
@RestController
class ValidationExceptionAdviceConfig : ResponseEntityExceptionHandler() {

    @ExceptionHandler(ValidationException::class)
    fun handleClientException(validationException: ValidationException, request: WebRequest) =
            ResponseEntity<Collection<ItemValidationError>>(validationException.itemValidationErrors, HttpStatus.PRECONDITION_FAILED)
}