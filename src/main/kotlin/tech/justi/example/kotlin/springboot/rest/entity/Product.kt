package tech.justi.example.kotlin.springboot.rest.entity

import javax.persistence.*


@Entity
data class Product(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,
        var name: String? = null,
        var description: String? = null,
        var value: Double? = null,
        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(name = "Product_Category", joinColumns = [JoinColumn(name = "product_id")], inverseJoinColumns = [JoinColumn(name = "category_id")])
        var categories: List<Category>? = null
)
