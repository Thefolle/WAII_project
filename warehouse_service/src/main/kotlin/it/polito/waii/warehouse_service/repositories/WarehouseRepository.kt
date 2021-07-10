package it.polito.waii.warehouse_service.repositories

import it.polito.waii.warehouse_service.entities.Warehouse
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WarehouseRepository : CrudRepository<Warehouse, Long> {
}