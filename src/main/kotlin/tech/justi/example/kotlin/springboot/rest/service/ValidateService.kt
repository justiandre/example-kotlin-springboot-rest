package tech.justi.example.kotlin.springboot.rest.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.RequestScope
import tech.justi.example.kotlin.springboot.rest.controller.config.ValidationExceptionAdviceConfig
import tech.justi.example.kotlin.springboot.rest.dto.ItemValidationError
import tech.justi.example.kotlin.springboot.rest.exception.ValidationException

@RequestScope
@Service
class ValidateService {

    companion object {
        const val ITEM_VALIDATION_ERROR_TYPE_REQUIRED = "required"
        const val ITEM_VALIDATION_ERROR_TYPE_MAX_SIZE = "maxSize"
        const val ITEM_VALIDATION_ERROR_TYPE_RELATIONSHIP = "relationship"
        const val ITEM_VALIDATION_ERROR_TYPE_NOT_NEGATIVE = "notNegative"
        const val ITEM_VALIDATION_ERROR_TYPE_DUPLICATE = "duplicate"
    }

    @Autowired
    private lateinit var messageSource: MessageSource

    private val logger = LoggerFactory.getLogger(ValidationExceptionAdviceConfig::class.java)
    private var itemValidationErrors: ArrayList<ItemValidationError> = arrayListOf()

    fun hasValidationError() = itemValidationErrors.isNotEmpty()

    fun addItemValidation(itemValidationError: ItemValidationError) {
        itemValidationError.apply { itemValidationErrors.add(this) }
    }

    fun addIfItemConditionIsTrue(condition: () -> Boolean, itemValidationError: ItemValidationError) {
        if (condition()) {
            addItemValidation(itemValidationError)
        }
    }

    fun addIfItemConditionIsTrue(condition: () -> Boolean, errorLocation: String, errorType: String, context: List<Any>? = null) {
        addIfItemConditionIsTrue(condition, createItemValidationError(errorLocation, errorType, context))
    }

    fun addIfItemConditionIsTrue(condition: Boolean, itemValidationError: ItemValidationError) {
        addIfItemConditionIsTrue({ condition }, itemValidationError)
    }

    fun addIfItemConditionIsTrue(condition: Boolean, errorLocation: String, errorType: String, context: List<Any>? = null) {
        addIfItemConditionIsTrue(condition, createItemValidationError(errorLocation, errorType, context))
    }


    fun addIfItemConditionIsTrueAndNotHasError(condition: () -> Boolean, errorLocation: String, errorType: String, context: List<Any>? = null) {
        if (!hasValidationError()) {
            addIfItemConditionIsTrue(condition, errorLocation, errorType, context)
        }
    }

    fun validate() {
        if (hasValidationError()) {
            throw ValidationException(itemValidationErrors)
        }
    }

    private fun createItemValidationError(errorLocation: String, errorType: String, context: List<Any>? = null) =
            ItemValidationError(errorLocation = errorLocation, errorType = errorType, context = context)
                    .apply { setMessageItemValidationError(this) }

    private fun setMessageItemValidationError(itemValidationError: ItemValidationError) =
            itemValidationError.apply { message = parseMessage(this) }

    private fun parseMessage(itemValidationError: ItemValidationError): String {
        val keyMessage = "${itemValidationError.errorLocation}.${itemValidationError.errorType}"
        try {
            return messageSource.getMessage(keyMessage, itemValidationError.context?.toTypedArray(), LocaleContextHolder.getLocale());
        } catch (noSuchMessageException: NoSuchMessageException) {
            logger.warn("Validation message not found: '$keyMessage'")
            return keyMessage
        }
    }
}
