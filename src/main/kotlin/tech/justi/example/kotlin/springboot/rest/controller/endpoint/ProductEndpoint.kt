package tech.justi.example.kotlin.springboot.rest.controller.endpointmessage

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.justi.example.kotlin.springboot.rest.entity.Product
import tech.justi.example.kotlin.springboot.rest.service.ProductService
import java.net.URI
import javax.servlet.http.HttpServletRequest

@RestController
class ProductEndpoint(
        @Autowired private val productService: ProductService
) {

    companion object {
        const val PARAM_PATH_ID: String = "id"
        const val URI_PATH_PRODUCT: String = "/product"
        const val URI_PATH_PRODUCT_ID: String = "$URI_PATH_PRODUCT/{$PARAM_PATH_ID}"
    }

    @GetMapping(URI_PATH_PRODUCT)
    fun findAll(@RequestParam(required = false) page: Int?, @RequestParam(required = false) maxRecords: Int?, @RequestParam(required = false) name: String?) = productService.findAll(page, maxRecords, name)

    @GetMapping(URI_PATH_PRODUCT_ID)
    fun findById(@PathVariable(PARAM_PATH_ID) id: Long) = productService.findById(id)

    @PostMapping(URI_PATH_PRODUCT)
    fun save(@RequestBody product: Product, request: HttpServletRequest): ResponseEntity<Any> {
        val id = productService.save(product.apply { id = null }).id
        return ResponseEntity.created(URI.create("${request.requestURL}/$id")).build()
    }

    @PutMapping(URI_PATH_PRODUCT_ID)
    fun edit(@PathVariable(PARAM_PATH_ID) id: Long, @RequestBody product: Product) =
            productService.save(product.apply { this.id = id })

    @DeleteMapping(URI_PATH_PRODUCT_ID)
    fun deleteById(@PathVariable(PARAM_PATH_ID) id: Long) = productService.delete(id)
}
