package tech.justi.example.kotlin.springboot.rest.service

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.CollectionUtils
import tech.justi.example.kotlin.springboot.rest.dto.ResultPage
import tech.justi.example.kotlin.springboot.rest.entity.Product
import tech.justi.example.kotlin.springboot.rest.repository.api.ProductRepository

@Service
class ProductService(
        @Autowired val paginationService: PaginationService,
        @Autowired val validationService: ValidateService,
        @Autowired val productRepository: ProductRepository
) {

    companion object {
        const val PRODUCT_NAME_MAX_SIZE = 70
        const val PRODUCT_DESCRIPTION_MAX_SIZE = 4000

        const val ITEM_VALIDATION_LOCATION_PRODUCT_NAME = "product.name"
        const val ITEM_VALIDATION_LOCATION_PRODUCT_DESCRIPTION = "product.description"
        const val ITEM_VALIDATION_LOCATION_PRODUCT_VALUE = "product.value"
        const val ITEM_VALIDATION_LOCATION_PRODUCT_CATEGORY = "product.category"
    }

    fun findById(id: Long) = productRepository.findById(id).orElse(null)

    fun findAll(page: Int?, maxRecords: Int?, name: String?): ResultPage<Product> {
        val find: (Pageable) -> Page<Product> = { productRepository.findAll(Example.of(Product(name = StringUtils.trimToNull(name))), it) }
        return paginationService.parseResult(page, maxRecords, find)
    }

    @Transactional
    fun save(product: Product) = product.let {
        validateSave(it)
        productRepository.save(it)
    }

    @Transactional
    fun delete(id: Long): Boolean {
        if (!productRepository.existsById(id)) {
            return false
        }
        productRepository.deleteById(id)
        return true
    }

    private fun validateSave(product: Product) = validationService.apply {
        addIfItemConditionIsTrue(StringUtils.isBlank(product.name), ITEM_VALIDATION_LOCATION_PRODUCT_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_REQUIRED)
        addIfItemConditionIsTrueAndNotHasError({ StringUtils.length(product.name) > PRODUCT_NAME_MAX_SIZE }, ITEM_VALIDATION_LOCATION_PRODUCT_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_MAX_SIZE, listOf(PRODUCT_NAME_MAX_SIZE))
        addIfItemConditionIsTrueAndNotHasError({ isDuplicateProduct(product) }, ITEM_VALIDATION_LOCATION_PRODUCT_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_DUPLICATE)
        addIfItemConditionIsTrue(StringUtils.length(product.description) > PRODUCT_DESCRIPTION_MAX_SIZE, ITEM_VALIDATION_LOCATION_PRODUCT_DESCRIPTION, ValidateService.ITEM_VALIDATION_ERROR_TYPE_MAX_SIZE, listOf(PRODUCT_DESCRIPTION_MAX_SIZE))
        addIfItemConditionIsTrue(CollectionUtils.isEmpty(product.categories), ITEM_VALIDATION_LOCATION_PRODUCT_CATEGORY, ValidateService.ITEM_VALIDATION_ERROR_TYPE_REQUIRED)
        product.value?.apply {
            addIfItemConditionIsTrue(this <= NumberUtils.DOUBLE_ZERO, ITEM_VALIDATION_LOCATION_PRODUCT_VALUE, ValidateService.ITEM_VALIDATION_ERROR_TYPE_NOT_NEGATIVE)
        }
    }.validate()

    fun isDuplicateProduct(product: Product) =
            product.id?.let {
                productRepository.existsByIdNotAndNameIgnoreCase(it, product.name)
            } ?: productRepository.existsByNameIgnoreCase(product.name)
}

