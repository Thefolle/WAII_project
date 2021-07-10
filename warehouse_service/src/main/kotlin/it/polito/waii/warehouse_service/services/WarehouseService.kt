package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.dtos.PartialWarehouseDto
import it.polito.waii.warehouse_service.dtos.WarehouseDto

interface WarehouseService {

    fun createWarehouse(warehouseDto: WarehouseDto): Long
    fun getWarehouses(): Set<WarehouseDto>
    fun getWarehouseById(id: Long): WarehouseDto
    fun updateWarehouse(id: Long, warehouseDto: WarehouseDto): Long?
    fun updateWarehouse(id: Long, warehouseDto: PartialWarehouseDto)
    fun deleteWarehouse(id: Long)

}