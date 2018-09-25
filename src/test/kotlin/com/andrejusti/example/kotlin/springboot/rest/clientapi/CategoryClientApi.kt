package com.andrejusti.example.kotlin.springboot.rest.clientapi

import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import com.andrejusti.example.kotlin.springboot.rest.controller.endpointmessage.CategoryEndpoint
import com.andrejusti.example.kotlin.springboot.rest.dto.Pagination
import com.andrejusti.example.kotlin.springboot.rest.dto.ResultPage
import com.andrejusti.example.kotlin.springboot.rest.entity.Category

@Component
class CategoryClientApi : AbstractClientApi() {

    fun findById(id: Long) =
            testRestTemplate.getForEntity("${CategoryEndpoint.URI_PATH_CATEGORY}/$id", Category::class.java)
                    ?.body

    fun findAll(pagination: Pagination): ResultPage<Category> {
        val queryParams = createPaginationQueryParams(pagination)
        val uri = UriComponentsBuilder.fromPath(CategoryEndpoint.URI_PATH_CATEGORY).queryParams(queryParams).toUriString()
        val body = testRestTemplate.getForObject(uri, String::class.java)
        return parseBodyGenerics<ResultPage<Category>>(body)
    }

    fun create(category: Category) =
            testRestTemplate.postForLocation(CategoryEndpoint.URI_PATH_CATEGORY, category)
                    ?.let { parseUriLocationId(it) }

    fun edit(category: Category) = testRestTemplate.put("${CategoryEndpoint.URI_PATH_CATEGORY}/${category.id}", category)

    fun delete(id: Long) = testRestTemplate.delete("${CategoryEndpoint.URI_PATH_CATEGORY}/$id")
}