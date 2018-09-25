package tech.justi.example.kotlin.springboot.rest.controller.endpointmessage

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tech.justi.example.kotlin.springboot.rest.entity.Category
import tech.justi.example.kotlin.springboot.rest.service.CategoryService
import java.net.URI
import javax.servlet.http.HttpServletRequest

@RestController
class CategoryEndpoint(
        @Autowired private val categoryService: CategoryService
) {

    companion object {
        const val PARAM_PATH_ID: String = "id"
        const val URI_PATH_CATEGORY: String = "/category"
        const val URI_PATH_CATEGORY_ID: String = "$URI_PATH_CATEGORY/{$PARAM_PATH_ID}"
    }

    @GetMapping(URI_PATH_CATEGORY)
    fun findAll(@RequestParam(required = false) page: Int?, @RequestParam(required = false) maxRecords: Int?) = categoryService.findAll(page, maxRecords)

    @GetMapping(URI_PATH_CATEGORY_ID)
    fun findById(@PathVariable(PARAM_PATH_ID) id: Long) = categoryService.findById(id)

    @PostMapping(URI_PATH_CATEGORY)
    fun save(@RequestBody category: Category, request: HttpServletRequest): ResponseEntity<Any> {
        val id = categoryService.save(category.apply { this.id = id }).id
        return ResponseEntity.created(URI.create("${request.requestURL}/$id")).build()
    }

    @PutMapping(URI_PATH_CATEGORY_ID)
    fun edit(@PathVariable(PARAM_PATH_ID) id: Long, @RequestBody category: Category) =
            categoryService.save(category.apply { this.id = id })

    @DeleteMapping(URI_PATH_CATEGORY_ID)
    fun deleteById(@PathVariable(PARAM_PATH_ID) id: Long) = categoryService.delete(id)
}
