package it.polito.waii.warehouse_service.controllers

import it.polito.waii.warehouse_service.dtos.PartialWarehouseDto
import it.polito.waii.warehouse_service.dtos.WarehouseDto
import it.polito.waii.warehouse_service.services.WarehouseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class WarehouseController {

    @Autowired
    lateinit var warehouseService: WarehouseService

    @PostMapping
    fun createWarehouse(@RequestBody warehouseDto: WarehouseDto): Long {
        return warehouseService
            .createWarehouse(warehouseDto)
    }

    @GetMapping
    fun getWarehouses(): Set<WarehouseDto> {
        return warehouseService
            .getWarehouses()
    }

    @GetMapping("/{id}")
    fun getWarehouseById(@PathVariable("id") id: Long): WarehouseDto {
        return warehouseService
            .getWarehouseById(id)
    }

    @PutMapping("/{id}")
    fun updateWarehouse(@PathVariable("id") id: Long, @RequestBody warehouseDto: WarehouseDto): Long? {
        return warehouseService
            .updateWarehouse(id, warehouseDto)
    }

    @PatchMapping("/{id}")
    fun updateWarehouse(@PathVariable("id") id: Long, @RequestBody warehouseDto: PartialWarehouseDto) {
        warehouseService
            .updateWarehouse(id, warehouseDto)
    }

    @DeleteMapping("/{id}")
    fun deleteWarehouse(@PathVariable("id") id: Long) {
        warehouseService
            .deleteWarehouse(id)
    }

}