package it.polito.waii.warehouse_service.repositories

import it.polito.waii.warehouse_service.entities.Warehouse
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface WarehouseRepository: ReactiveNeo4jRepository<Warehouse, Long> {
    fun findByWarehousename(productName: String): Warehouse
    fun getProductList(): Set<String>
    fun findByName(name: String):Warehouse
}