package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.dtos.*
import it.polito.waii.warehouse_service.entities.*
import it.polito.waii.warehouse_service.repositories.ProductRepository
import it.polito.waii.warehouse_service.repositories.ProductWarehouseRepository
import it.polito.waii.warehouse_service.repositories.WarehouseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class WarehouseServiceImpl : WarehouseService {

    @Autowired
    lateinit var warehouseRepository: WarehouseRepository

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var productWarehouseRepository: ProductWarehouseRepository


    private fun getProductWarehouseById(productId: Long, warehouseId: Long): ProductWarehouse{
        val compKey = CompositeKey(productId, warehouseId)
        val productWarehouseOptional = productWarehouseRepository.findById(compKey)
        if (productWarehouseOptional.isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No product with id $productId exists inside warehouse with id $warehouseId."
        )
        return productWarehouseOptional.get()
    }

    override fun createWarehouse(warehouseDto: WarehouseDto): Long {
        return warehouseRepository.save(
            Warehouse(
                null,
                warehouseDto.name,
                warehouseDto.city,
                warehouseDto.region,
                warehouseDto.capacity,
                null
            )
        )
            .id!!
    }


    override fun getWarehouses(): Set<WarehouseDto> {
        return warehouseRepository
            .findAll()
            .map {
                it.toDto()
            }
            .toSet()
    }


    override fun getWarehouseById(id: Long): WarehouseDto {
        return warehouseRepository
            .findById(id)
            .get()
            .toDto()
    }


    override fun updateWarehouse(id: Long, warehouseDto: WarehouseDto): Long? {
        return if (warehouseRepository.existsById(id)) {
            warehouseRepository.save(
                Warehouse(
                    id,
                    warehouseDto.name,
                    warehouseDto.city,
                    warehouseDto.region,
                    warehouseDto.capacity,
                    null
                )
            )
            null
        } else {
            warehouseRepository.save(
                Warehouse(
                    null,
                    warehouseDto.name,
                    warehouseDto.city,
                    warehouseDto.region,
                    warehouseDto.capacity,
                    null
                )
            )
                .id
        }
    }


    override fun updateWarehouse(id: Long, warehouseDto: PartialWarehouseDto) {
        val warehouse = warehouseRepository.findById(id).get()

        if (warehouseDto.name != null) {
            warehouse.name = warehouseDto.name!!
        }
        if (warehouseDto.city != null) {
            warehouse.city = warehouseDto.city!!
        }
        if (warehouseDto.region != null) {
            warehouse.region = warehouseDto.region!!
        }
        if (warehouseDto.capacity != null) {
            warehouse.capacity = warehouseDto.capacity!!
        }

    }


    override fun deleteWarehouse(id: Long) {
        warehouseRepository
            .deleteById(id)
    }

    override fun getProductQuantity(warehouseId: Long, productId: Long): Long {
        if (productRepository.findById(productId).isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No product with id $productId exists."
        )
        if (warehouseRepository.findById(warehouseId).isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No warehouse with id $warehouseId exists."
        )

        return getProductWarehouseById(productId, warehouseId).quantity
    }

    override fun getAllQuantities(warehouseId: Long): List<ProductQuantityDTO> {
        if (warehouseRepository.findById(warehouseId).isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No warehouse with id $warehouseId exists."
        )
        return productWarehouseRepository.findAll().map { ProductQuantityDTO(it.product.name, it.quantity) }
    }

    override fun updateProductQuantity(warehouseId: Long, updateQuantityDTO: UpdateQuantityDTO): ProductWarehouseDTO {
        if (warehouseRepository.findById(warehouseId).isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No warehouse with id $warehouseId exists."
        )
        val compKey = CompositeKey(updateQuantityDTO.productId, warehouseId)
        val productWarehouseOptional = productWarehouseRepository.findById(compKey)
        // if I need to add the quantity...
        if (updateQuantityDTO.action == Action.ADD){
            if (productWarehouseOptional.isEmpty){
                //...and if I don't have the relation, I create a new ProductWarehouse with the specified quantity (if product exists)
                val productOptional = productRepository.findById(updateQuantityDTO.productId)
                if (productOptional.isEmpty) throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No product with id ${updateQuantityDTO.productId} exists."
                )
                val product = productOptional.get()
                val warehouse = warehouseRepository.findById(warehouseId).get()
                // I Chose 10 as a default alarm level (can be modified with another API)
                val newProductWarehouse = ProductWarehouse(compKey, product, warehouse, updateQuantityDTO.quantity, 10)
                productWarehouseRepository.save(newProductWarehouse)
            }
            else{
                //...if it already existed I simply add the quantity
                val productWarehouse = productWarehouseOptional.get()
                productWarehouse.quantity += updateQuantityDTO.quantity
            }
        }

        // if I need to sub the quantity...
        else{
            //...and if I don't have the relation, I just throw an exception
            if (productWarehouseOptional.isEmpty) throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No product with id ${updateQuantityDTO.productId} exists inside warehouse with id $warehouseId."
            )
            //else I simply sub the specified quantity and check the alarm level
            val productWarehouse = productWarehouseOptional.get()
            if (productWarehouse.quantity < updateQuantityDTO.quantity) throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Not enough products!"
            )

            productWarehouse.quantity -= updateQuantityDTO.quantity
            if (productWarehouse.quantity < productWarehouse.alarmLevel){
                //Todo: send email to admins
            }
        }

         return productWarehouseRepository.findById(compKey).get().toDTO()
    }

    override fun updateProductAlarmLevel(warehouseId: Long, productId: Long, newAlarmLevel: Long): ProductWarehouseDTO {
        val productWarehouse = getProductWarehouseById(productId, warehouseId)
        productWarehouse.alarmLevel = newAlarmLevel
        if (productWarehouse.quantity < productWarehouse.alarmLevel){
            //Todo: send email to admins
        }

        return productWarehouse.toDTO()
    }

}
