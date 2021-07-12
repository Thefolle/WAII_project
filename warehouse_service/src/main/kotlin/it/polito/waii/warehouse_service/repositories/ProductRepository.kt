package it.polito.waii.warehouse_service.repositories

import it.polito.waii.warehouse_service.entities.Product
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository: CrudRepository<Product, Long> {
    fun findByCategory(category: String): MutableList<Product>
}
