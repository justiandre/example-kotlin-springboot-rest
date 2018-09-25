package tech.justi.example.kotlin.springboot.rest.clientapi

import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import tech.justi.example.kotlin.springboot.rest.controller.endpointmessage.ProductEndpoint
import tech.justi.example.kotlin.springboot.rest.dto.Pagination
import tech.justi.example.kotlin.springboot.rest.dto.ResultPage
import tech.justi.example.kotlin.springboot.rest.entity.Product


@Component
class ProductClientApi : AbstractClientApi() {

    companion object {
        const val QUERY_PARAM_KEY_PAGINATION_NAME: String = "name"
    }

    fun findById(id: Long) =
            testRestTemplate.getForEntity("${ProductEndpoint.URI_PATH_PRODUCT}/$id", Product::class.java)
                    ?.body

    fun findAll(pagination: Pagination, name: String): ResultPage<Product> {
        val queryParams = createPaginationQueryParams(pagination, mapOf(QUERY_PARAM_KEY_PAGINATION_NAME to name))
        val uri = UriComponentsBuilder.fromPath(ProductEndpoint.URI_PATH_PRODUCT).queryParams(queryParams).toUriString()
        val body = testRestTemplate.getForObject(uri, String::class.java)
        return parseBodyGenerics<ResultPage<Product>>(body)
    }

    fun create(product: Product) =
            testRestTemplate.postForLocation(ProductEndpoint.URI_PATH_PRODUCT, product)
                    ?.let { parseUriLocationId(it) }

    fun edit(product: Product) = testRestTemplate.put("${ProductEndpoint.URI_PATH_PRODUCT}/${product.id}", product)

    fun delete(id: Long) = testRestTemplate.delete("${ProductEndpoint.URI_PATH_PRODUCT}/$id")
}