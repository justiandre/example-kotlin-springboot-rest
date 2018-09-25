package com.andrejusti.example.kotlin.springboot.rest.clientapi

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.DefaultResponseErrorHandler
import com.andrejusti.example.kotlin.springboot.rest.dto.ItemValidationError
import com.andrejusti.example.kotlin.springboot.rest.dto.Pagination
import com.andrejusti.example.kotlin.springboot.rest.exception.ValidationException
import java.net.URI
import javax.annotation.PostConstruct


@Component
abstract class AbstractClientApi {

    companion object {
        const val QUERY_PARAM_KEY_PAGINATION_PAGE = "page"
        const val QUERY_PARAM_KEY_PAGINATION_SIZE = "maxRecords"
    }

    @Autowired
    protected lateinit var objectMapperJson: ObjectMapper

    @Autowired
    protected lateinit var testRestTemplate: TestRestTemplate

    @PostConstruct
    fun configuration() {
        testRestTemplate.restTemplate.errorHandler = creteResponseErrorHandler()
    }

    protected fun createPaginationQueryParams(pagination: Pagination, others: Map<String, Any>? = null) =
            LinkedMultiValueMap<String, String>().apply {
                pagination.page?.run { add(QUERY_PARAM_KEY_PAGINATION_PAGE, this.toString()) }
                pagination.maxRecords?.run { add(QUERY_PARAM_KEY_PAGINATION_SIZE, this.toString()) }
                others?.mapNotNull { add(it.key, it.value.toString()) }
            }

    protected fun parseUriLocationId(uri: URI) =
            StringUtils.substringAfterLast(uri.toString(), "/")?.let { it.toLong() }

    private fun creteResponseErrorHandler() = object : DefaultResponseErrorHandler() {
        override fun handleError(response: ClientHttpResponse, statusCode: HttpStatus) {
            if (statusCode != HttpStatus.PRECONDITION_FAILED) {
                super.handleError(response, statusCode)
                return
            }
            response.body.use {
                val itemValidationError = objectMapperJson.reader().forType(object : TypeReference<List<ItemValidationError>>() {}).readValue<List<ItemValidationError>>(it)
                throw ValidationException(itemValidationError)
            }
        }
    }

    protected final inline fun <reified T> parseBodyGenerics(body: String?): T {
        return objectMapperJson.reader().forType(object : TypeReference<T>() {}).readValue(body)
    }
}