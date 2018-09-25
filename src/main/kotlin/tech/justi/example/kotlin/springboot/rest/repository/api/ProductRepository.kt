package tech.justi.example.kotlin.springboot.rest.repository.api

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import tech.justi.example.kotlin.springboot.rest.entity.Product

@Repository
interface ProductRepository : JpaRepository<Product, Long> {

    fun existsByCategoriesId(id: Long): Boolean

    fun existsByNameIgnoreCase(name: String?): Boolean

    fun existsByIdNotAndNameIgnoreCase(id: Long, name: String?): Boolean
}