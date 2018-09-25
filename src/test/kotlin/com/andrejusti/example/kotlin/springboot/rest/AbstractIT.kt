package com.andrejusti.example.kotlin.springboot.rest

import org.apache.commons.lang3.math.NumberUtils
import org.junit.Assert
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import com.andrejusti.example.kotlin.springboot.rest.dto.Pagination
import com.andrejusti.example.kotlin.springboot.rest.exception.ValidationException
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractIT {

    protected fun createRandomValue() = UUID.randomUUID().toString()

    protected fun createPagination() = Pagination(page = NumberUtils.INTEGER_ZERO, maxRecords = Int.MAX_VALUE)

    protected fun assertValidationException(errorLocation: String, errorType: String, exec: () -> Unit) {
        try {
            exec()
            Assert.fail("Not generated validation exception")
        } catch (validationException: ValidationException) {
            val existErro = validationException.itemValidationErrors.any { it.errorLocation == errorLocation && it.errorType == errorType }
            Assert.assertTrue("Invalid validation exception", existErro)
        }
    }
}