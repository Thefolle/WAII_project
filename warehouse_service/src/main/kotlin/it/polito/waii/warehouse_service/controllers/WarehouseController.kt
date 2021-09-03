package it.polito.waii.warehouse_service.controllers

import it.polito.waii.warehouse_service.dtos.*
import it.polito.waii.warehouse_service.services.WarehouseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

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
    fun updateWarehouse(@PathVariable("id") id: Long, @RequestBody warehouseDto: WarehouseDto): String {
        val idOrNull = warehouseService
            .updateWarehouse(id, warehouseDto)

        return if (idOrNull == null) "The warehouse has been correctly updated." else "A new warehouse with id $idOrNull has been created."
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

    @GetMapping("/{id}/quantity/{productId}")
    fun getProductQuantity(@PathVariable("id") warehouseId: Long, @PathVariable("productId") productId: Long): ResponseEntity<Long> {
        return ResponseEntity.status(HttpStatus.OK).body(warehouseService.getProductQuantity(warehouseId,productId))
    }

    @GetMapping("/{id}/quantity")
    fun getAllQuantities(@PathVariable("id") warehouseId: Long): ResponseEntity<List<ProductQuantityDTO>> {
        return ResponseEntity.status(HttpStatus.OK).body(warehouseService.getAllQuantities(warehouseId))
    }

    @PutMapping("/{id}/quantity")
    fun updateProductQuantity(@PathVariable("id") warehouseId: Long, @RequestBody updateQuantityDTO: UpdateQuantityDTO): ResponseEntity<String> {
        if (updateQuantityDTO.quantity < 0) throw ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Quantity should be positive!"
        )
        warehouseService.updateProductQuantity(warehouseId, updateQuantityDTO)
        return ResponseEntity.status(HttpStatus.OK).body("The quantity has been correctly updated")
    }

    @PutMapping("/{id}/alarm/{productId}")
    fun updateProductAlarmLevel(@PathVariable("id") warehouseId: Long, @PathVariable("productId") productId: Long, @RequestBody newAlarmLevel: Long): ResponseEntity<ProductWarehouseDTO> {
        if (newAlarmLevel < 0) throw ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Alarm level cannot be negative!"
        )
        return ResponseEntity.status(HttpStatus.OK).body(warehouseService.updateProductAlarmLevel(warehouseId, productId, newAlarmLevel))
    }

}
