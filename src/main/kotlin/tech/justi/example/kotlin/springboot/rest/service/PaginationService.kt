package tech.justi.example.kotlin.springboot.rest.service

import org.apache.commons.lang3.math.NumberUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import tech.justi.example.kotlin.springboot.rest.dto.ResultPage

@Service
class PaginationService(
        @Value("\${app.search.pagination.numberRecords.default}") val paginationNumberRecordsDefault: Int,
        @Value("\${app.search.pagination.numberRecords.max}") val paginationNumberRecordsMax: Int
) {

    fun parsePagination(page: Int?, maxRecords: Int?): Pageable {
        val pageNormalized = page ?: NumberUtils.INTEGER_ZERO
        val maxRecordsNormalized: Int = (maxRecords ?: paginationNumberRecordsDefault)
                .takeIf { paginationNumberRecordsMax > it }
                ?: paginationNumberRecordsMax
        return PageRequest.of(pageNormalized, maxRecordsNormalized)
    }

    fun <T> parseResult(page: Int?, maxRecords: Int?, find: (Pageable) -> Page<T>): ResultPage<T> {
        val pageable = parsePagination(page, maxRecords)
        val page = find(pageable)
        return ResultPage(page.totalElements, page.content)
    }
}


