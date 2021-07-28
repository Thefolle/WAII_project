package it.polito.waii.warehouse_service.repositories

import it.polito.waii.warehouse_service.entities.CompositeKey
import it.polito.waii.warehouse_service.entities.Product
import it.polito.waii.warehouse_service.entities.ProductWarehouse
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductWarehouseRepository : CrudRepository<ProductWarehouse, CompositeKey> {

    fun getAllByProductId(productId: Long): List<ProductWarehouse>

    fun getAllByProduct(product: Product): List<ProductWarehouse>

    fun findByCompositeKey(compositeKey: CompositeKey): Optional<ProductWarehouse>

}