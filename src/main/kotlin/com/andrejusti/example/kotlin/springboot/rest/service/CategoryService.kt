package com.andrejusti.example.kotlin.springboot.rest.service

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.andrejusti.example.kotlin.springboot.rest.entity.Category
import com.andrejusti.example.kotlin.springboot.rest.repository.api.CategoryRepository
import com.andrejusti.example.kotlin.springboot.rest.repository.api.ProductRepository

@Service
class CategoryService(
        @Autowired val paginationService: PaginationService,
        @Autowired val validationService: ValidateService,
        @Autowired val categoryRepository: CategoryRepository,
        @Autowired val productRepository: ProductRepository
) {

    companion object {
        const val CATEGORY_NAME_MAX_SIZE = 70

        const val ITEM_VALIDATION_LOCATION_CATEGORY_NAME = "category.name"
        const val ITEM_VALIDATION_LOCATION_CATEGORY_PRODUCT = "category.product"
    }

    fun findById(id: Long) = categoryRepository.findById(id).orElse(null)

    fun findAll(page: Int?, maxRecords: Int?) =
            paginationService.parseResult(page, maxRecords, categoryRepository::findAll)

    @Transactional
    fun save(category: Category) = category.let {
        validateSave(it)
        categoryRepository.save(it)
    }

    @Transactional
    fun delete(id: Long) {
        if (!categoryRepository.existsById(id)) {
            return
        }
        validateDelete(id)
        categoryRepository.deleteById(id)
    }

    private fun validateDelete(idCategory: Long) = validationService.apply {
        addIfItemConditionIsTrue(productRepository.existsByCategoriesId(idCategory), ITEM_VALIDATION_LOCATION_CATEGORY_PRODUCT, ValidateService.ITEM_VALIDATION_ERROR_TYPE_RELATIONSHIP)
    }.validate()

    private fun validateSave(category: Category) = validationService.apply {
        addIfItemConditionIsTrue(StringUtils.isBlank(category.name), ITEM_VALIDATION_LOCATION_CATEGORY_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_REQUIRED)
        addIfItemConditionIsTrueAndNotHasError({ StringUtils.length(category.name) > CATEGORY_NAME_MAX_SIZE }, ITEM_VALIDATION_LOCATION_CATEGORY_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_MAX_SIZE, listOf(CATEGORY_NAME_MAX_SIZE))
        addIfItemConditionIsTrueAndNotHasError({ isDuplicateCategory(category) }, ITEM_VALIDATION_LOCATION_CATEGORY_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_DUPLICATE)
    }.validate()

    private fun isDuplicateCategory(category: Category) =
            category.id?.let {
                categoryRepository.existsByIdNotAndNameIgnoreCase(it, category.name)
            } ?: categoryRepository.existsByNameIgnoreCase(category.name)
}
