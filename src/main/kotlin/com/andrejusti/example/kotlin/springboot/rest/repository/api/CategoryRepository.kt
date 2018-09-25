package com.andrejusti.example.kotlin.springboot.rest.repository.api

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import com.andrejusti.example.kotlin.springboot.rest.entity.Category

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {

    fun existsByNameIgnoreCase(name: String?): Boolean

    fun existsByIdNotAndNameIgnoreCase(id: Long, name: String?): Boolean
}


