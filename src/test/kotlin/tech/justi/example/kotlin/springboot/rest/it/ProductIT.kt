package tech.justi.example.kotlin.springboot.rest.it

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.junit.Assert
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import tech.justi.example.kotlin.springboot.rest.AbstractIT
import tech.justi.example.kotlin.springboot.rest.clientapi.CategoryClientApi
import tech.justi.example.kotlin.springboot.rest.clientapi.ProductClientApi
import tech.justi.example.kotlin.springboot.rest.entity.Category
import tech.justi.example.kotlin.springboot.rest.entity.Product
import tech.justi.example.kotlin.springboot.rest.service.ProductService
import tech.justi.example.kotlin.springboot.rest.service.ValidateService

class ProductIT : AbstractIT() {

    @Autowired
    lateinit var productClientApi: ProductClientApi

    @Autowired
    lateinit var categoryClientApi: CategoryClientApi

    @Test
    fun `Search product by id without expecting result`() {
        val product = productClientApi.findById(Int.MAX_VALUE.toLong())
        Assert.assertNull("Should not return findById", product)
    }

    @Test
    fun `Search products without expecting result`() {
        val products = productClientApi.findAll(createPagination(), createRandomValue())
        Assert.assertNotNull("Should not return null", products)
    }

    @Test
    fun `Search products by name expecting results`() {
        val product = createAndSaveProduct()
        val productSearch = productClientApi.findAll(createPagination(), product.name!!).records?.firstOrNull { it.id == product.id }
        Assert.assertEquals("Product retrieved is different from saved", product, productSearch)
    }

    @Test
    fun `Delete nonexistent product`() {
        productClientApi.delete(Int.MAX_VALUE.toLong())
    }

    @Test
    fun `Create product checking id`() {
        val product = createAndSaveProduct()
        Assert.assertNotNull("Should return id", product.id)
    }

    @Test
    fun `Delete product checking find by id`() {
        assertDeleteProduct(productClientApi::findById)
    }

    @Test
    fun `Delete product checking find all`() {
        assertDeleteProduct(::findAllProductId)
    }

    @Test
    fun `Create product checking find by id`() {
        assertCreateProduct(productClientApi::findById)
    }

    @Test
    fun `Create product checking find all`() {
        assertCreateProduct(::findAllProductId)
    }

    @Test
    fun `Edit product checking find by id`() {
        assertEditProduct(productClientApi::findById)
    }

    @Test
    fun `Edit product checking find all`() {
        assertEditProduct(::findAllProductId)
    }

    @Test
    fun `Create product checking validation - product duplicate`() {
        val product = createProduct()
        productClientApi.create(product)
        assertValidationException(ProductService.ITEM_VALIDATION_LOCATION_PRODUCT_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_DUPLICATE, { productClientApi.create(product) })
    }

    @Test
    fun `Edit product checking validation - product duplicate`() {
        val product1 = createAndSaveProduct()
        val product2 = createAndSaveProduct()
        product1.name = product2.name
        assertValidationException(ProductService.ITEM_VALIDATION_LOCATION_PRODUCT_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_DUPLICATE, { productClientApi.edit(product1) })
    }

    @Test
    fun `Create product checking validation - product name not black`() {
        assertValidationExceptionProductNameNotBlack(createProduct(), { productClientApi.create(it) })
    }

    @Test
    fun `Edit product checking validation - product name not black`() {
        assertValidationExceptionProductNameNotBlack(createAndSaveProduct(), { productClientApi.edit(it) })
    }

    @Test
    fun `Create product checking validation - product name max size`() {
        assertValidationExceptionProductNameMaxSize(createProduct(), { productClientApi.create(it) })
    }

    @Test
    fun `Edit product checking validation - product name max size`() {
        assertValidationExceptionProductNameMaxSize(createAndSaveProduct(), { productClientApi.edit(it) })
    }

    @Test
    fun `Create product checking validation - product description max size`() {
        assertValidationExceptionProductDescriptionMaxSize(createProduct(), { productClientApi.create(it) })
    }

    @Test
    fun `Edit product checking validation - product description max size`() {
        assertValidationExceptionProductDescriptionMaxSize(createAndSaveProduct(), { productClientApi.edit(it) })
    }

    @Test
    fun `Create product checking validation - product value not negative`() {
        assertValidationExceptionProductValueNotNegative(createProduct(), { productClientApi.create(it) })
    }

    @Test
    fun `Edit product checking validation - product value not negative`() {
        assertValidationExceptionProductValueNotNegative(createAndSaveProduct(), { productClientApi.edit(it) })
    }

    @Test
    fun `Create product checking validation - product category required`() {
        assertValidationExceptionProductCategoryRequired(createProduct(), { productClientApi.create(it) })
    }

    @Test
    fun `Edit product checking validation - product category required`() {
        assertValidationExceptionProductCategoryRequired(createAndSaveProduct(), { productClientApi.edit(it) })
    }

    private fun findAllProductId(productId: Long) = productClientApi.findAll(createPagination(), StringUtils.EMPTY).records?.firstOrNull { it.id == productId }

    private fun createAndSaveCategory() = createCategory().apply { id = categoryClientApi.create(this) }

    private fun createCategory() = Category(name = createRandomValue())

    private fun createAndSaveProduct() = createProduct().apply { id = productClientApi.create(this) }

    private fun createProduct() = Product(
            name = createRandomValue(),
            description = createRandomValue(),
            value = NumberUtils.DOUBLE_ONE,
            categories = listOf(createAndSaveCategory())
    )

    private fun assertDeleteProduct(searchProduct: (Long) -> Product?) {
        val product = assertCreateProduct(searchProduct)
        productClientApi.delete(product.id!!)
        val productSearchAfterDelete = searchProduct(product.id!!)
        Assert.assertNull("Should not return to findById after deleting", productSearchAfterDelete)
    }

    private fun assertEditProduct(searchProduct: (Long) -> Product?) {
        val product = assertCreateProduct(searchProduct)
        product.name = createRandomValue()
        productClientApi.edit(product)
        val productSearch = searchProduct(product.id!!)
        Assert.assertEquals("Product retrieved is different from edited", product, productSearch)
    }

    private fun assertCreateProduct(searchProduct: (Long) -> Product?): Product {
        val product = createAndSaveProduct()
        val productId = product.id
        Assert.assertNotNull("Should return id", productId)
        val productSearch = searchProduct(productId!!)
        Assert.assertEquals("Product retrieved is different from saved", product, productSearch)
        return productSearch!!
    }

    private fun assertValidationExceptionProductNameNotBlack(product: Product, execValidationException: (Product) -> Unit) {
        product.name = StringUtils.EMPTY
        val exec = { execValidationException(product) }
        assertValidationException(ProductService.ITEM_VALIDATION_LOCATION_PRODUCT_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_REQUIRED, exec)
    }

    private fun assertValidationExceptionProductNameMaxSize(product: Product, execValidationException: (Product) -> Unit) {
        product.name = StringUtils.repeat("A", ProductService.PRODUCT_NAME_MAX_SIZE + NumberUtils.INTEGER_ONE)
        val exec = { execValidationException(product) }
        assertValidationException(ProductService.ITEM_VALIDATION_LOCATION_PRODUCT_NAME, ValidateService.ITEM_VALIDATION_ERROR_TYPE_MAX_SIZE, exec)
    }

    private fun assertValidationExceptionProductDescriptionMaxSize(product: Product, execValidationException: (Product) -> Unit) {
        product.description = StringUtils.repeat("A", ProductService.PRODUCT_DESCRIPTION_MAX_SIZE + NumberUtils.INTEGER_ONE)
        val exec = { execValidationException(product) }
        assertValidationException(ProductService.ITEM_VALIDATION_LOCATION_PRODUCT_DESCRIPTION, ValidateService.ITEM_VALIDATION_ERROR_TYPE_MAX_SIZE, exec)
    }

    private fun assertValidationExceptionProductValueNotNegative(product: Product, execValidationException: (Product) -> Unit) {
        product.value = NumberUtils.DOUBLE_MINUS_ONE
        val exec = { execValidationException(product) }
        assertValidationException(ProductService.ITEM_VALIDATION_LOCATION_PRODUCT_VALUE, ValidateService.ITEM_VALIDATION_ERROR_TYPE_NOT_NEGATIVE, exec)
    }

    private fun assertValidationExceptionProductCategoryRequired(product: Product, execValidationException: (Product) -> Unit) {
        product.categories = null
        val exec = { execValidationException(product) }
        assertValidationException(ProductService.ITEM_VALIDATION_LOCATION_PRODUCT_CATEGORY, ValidateService.ITEM_VALIDATION_ERROR_TYPE_REQUIRED, exec)
    }
}

