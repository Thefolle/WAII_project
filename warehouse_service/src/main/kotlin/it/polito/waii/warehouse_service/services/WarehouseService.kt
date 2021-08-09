package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.dtos.*

interface WarehouseService {

    fun createWarehouse(warehouseDto: WarehouseDto): Long
    fun getWarehouses(): Set<WarehouseDto>
    fun getWarehouseById(id: Long): WarehouseDto
    fun updateWarehouse(id: Long, warehouseDto: WarehouseDto): Long?
    fun updateWarehouse(id: Long, warehouseDto: PartialWarehouseDto)
    fun deleteWarehouse(id: Long)
    fun getProductQuantity(warehouseId: Long, productId: Long): Long
    fun getAllQuantities(warehouseId: Long): List<ProductQuantityDTO>
    fun updateProductQuantity(warehouseId: Long, updateQuantityDTO: UpdateQuantityDTO): ProductWarehouseDTO
    fun updateProductQuantities(updateQuantitiesDto: Set<UpdateQuantityDtoKafka>)
    fun updateProductAlarmLevel(warehouseId: Long, productId: Long, newAlarmLevel: Long): ProductWarehouseDTO

}
