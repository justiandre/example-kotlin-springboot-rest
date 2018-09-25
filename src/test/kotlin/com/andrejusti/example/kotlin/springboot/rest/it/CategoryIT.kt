package com.andrejusti.example.kotlin.springboot.rest.it

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import com.andrejusti.example.kotlin.springboot.rest.AbstractIT
import com.andrejusti.example.kotlin.springboot.rest.clientapi.CategoryClientApi
import com.andrejusti.example.kotlin.springboot.rest.clientapi.ProductClientApi
import com.andrejusti.example.kotlin.springboot.rest.dto.Pagination
import com.andrejusti.example.kotlin.springboot.rest.entity.Category
import com.andrejusti.example.kotlin.springboot.rest.entity.Product
import com.andrejusti.example.kotlin.springboot.rest.service.CategoryService
import com.andrejusti.example.kotlin.springboot.rest.service.ValidateService

class CategoryIT : AbstractIT() {

    @Autowired
    lateinit var productClientApi: ProductClientApi

    @Autowired
    lateinit var categoryClientApi: CategoryClientApi

    @Test
    fun `Search find all without informing pagination`() {
        categoryClientApi.findAll(Pagination())
    }

    @Test
    fun `Search category by id without expecting result`() {
        val category = categoryClientApi.findById(Int.MAX_VALUE.toLong())
        Assert.assertNull("Should not return findById", category)
    }


    @Test
    fun `Search categories without expecting result`() {
        val categories = categoryClientApi.findAll(createPagination())
        Assert.assertNotNull("Should not return null", categories)
    }

    @Test
    fun `Delete nonexistent category`() {
        categoryClientApi.delete(Int.MAX_VALUE.toLong())
    }

    @Test
    fun `Create category checking id`() {
        val category = createAndSaveCategory()
        Assert.assertNotNull("Should return id", category.id)
    }

    @Test
    fun `Delete category checking find by id`() {
        assertDeleteCategory(categoryClientApi::findById)
    }

    @Test
    fun `Delete category checking find all`() {
        assertDeleteCategory(::findAllCategoryId)
    }

    @Test
    fun `Delete category checking validation - category product relationship`() {
        val category = createAndSaveCategory()
        productClientApi.create(Product(name = createRandomValue(), categories = listOf(category)))
        val execValidationException: () -> Unit = { categoryClientApi.delete(category.id!!) }
        assertValidationException(CategoryService.ITEM_VALIDATION_LOCATION_CATEGORY_PRODUCT, ValidateService.ITEM_VALIDATION_ERROR_TYPE_RELATIONSHIP, execValidationException)
    }

    @Test
    fun `Create category checking find by id`() {
        assertCreateCategory(categoryClientApi::findById)
    }

    @Test
    fun `Create category checking find all`() {
        assertCreateCategory(::findAllCategoryId)
    }

    @Test
    fun `Edit category checking find by id`() {
        assertEditCategory(categoryClientApi::findById)
    }

    @Test
    fun `Edit category checking find all`() {
        assertEditCategory(::findAllCategoryId)
    }

    @Test
    fun `Create category checking validation - category duplicate`() {
        val category = createCategory()
        categoryClientApi.create(category)
        assertValidationException(CategoryService.ITEM_VALIDATION_LOCATION_CATEGORY_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_DUPLICATE, { categoryClientApi.create(category) })
    }

    @Test
    fun `Edit category checking validation - category duplicate`() {
        val category1 = createAndSaveCategory()
        val category2 = createAndSaveCategory()
        category1.name = category2.name
        assertValidationException(CategoryService.ITEM_VALIDATION_LOCATION_CATEGORY_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_DUPLICATE, { categoryClientApi.edit(category1) })
    }

    @Test
    fun `Create category checking validation - category name not black`() {
        assertValidationExceptionCategoryNameNotBlack(createCategory(), { categoryClientApi.create(it) })
    }

    @Test
    fun `Edit category checking validation - category name not black`() {
        assertValidationExceptionCategoryNameNotBlack(createAndSaveCategory(), { categoryClientApi.edit(it) })
    }

    @Test
    fun `Create category checking validation - category name max size`() {
        assertValidationExceptionCategoryNameMaxSize(createCategory(), { categoryClientApi.create(it) })
    }

    @Test
    fun `Edit category checking validation - category name max size`() {
        assertValidationExceptionCategoryNameMaxSize(createAndSaveCategory(), { categoryClientApi.edit(it) })
    }

    private fun findAllCategoryId(categoryId: Long) = categoryClientApi.findAll(createPagination()).records?.firstOrNull { it.id == categoryId }

    private fun assertDeleteCategory(searchCategory: (Long) -> Category?) {
        val category = assertCreateCategory(searchCategory)
        categoryClientApi.delete(category.id!!)
        val categorySearchAfterDelete = searchCategory(category.id!!)
        Assert.assertNull("Should not return to findById after deleting", categorySearchAfterDelete)
    }

    private fun assertEditCategory(searchCategory: (Long) -> Category?) {
        val category = assertCreateCategory(searchCategory)
        category.name = createRandomValue()
        categoryClientApi.edit(category)
        val categorySearch = searchCategory(category.id!!)
        Assert.assertEquals("Category retrieved is different from edited", category, categorySearch)
    }

    private fun assertCreateCategory(searchCategory: (Long) -> Category?): Category {
        val category = createAndSaveCategory()
        val categoryId = category.id
        Assert.assertNotNull("Should return id", categoryId)
        val categorySearch = searchCategory(categoryId!!)
        Assert.assertEquals("Category retrieved is different from saved", category, categorySearch)
        return categorySearch!!
    }

    private fun assertValidationExceptionCategoryNameNotBlack(category: Category, execValidationException: (Category) -> Unit) {
        category.name = StringUtils.EMPTY
        val exec = { execValidationException(category) }
        assertValidationException(CategoryService.ITEM_VALIDATION_LOCATION_CATEGORY_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_REQUIRED, exec)
    }

    private fun assertValidationExceptionCategoryNameMaxSize(category: Category, execValidationException: (Category) -> Unit) {
        category.name = StringUtils.repeat("A", CategoryService.CATEGORY_NAME_MAX_SIZE + NumberUtils.INTEGER_ONE)
        val exec = { execValidationException(category) }
        assertValidationException(CategoryService.ITEM_VALIDATION_LOCATION_CATEGORY_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_MAX_SIZE, exec)
    }

    private fun createAndSaveCategory() = createCategory().apply { id = categoryClientApi.create(this) }

    private fun createCategory() = Category(name = createRandomValue())
}

