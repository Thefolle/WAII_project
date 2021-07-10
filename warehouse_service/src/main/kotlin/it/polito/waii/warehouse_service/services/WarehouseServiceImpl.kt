package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.dtos.PartialWarehouseDto
import it.polito.waii.warehouse_service.dtos.WarehouseDto
import it.polito.waii.warehouse_service.entities.Warehouse
import it.polito.waii.warehouse_service.repositories.WarehouseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WarehouseServiceImpl : WarehouseService {

    @Autowired
    lateinit var warehouseRepository: WarehouseRepository

    @Transactional
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

    @Transactional
    override fun getWarehouses(): Set<WarehouseDto> {
        return warehouseRepository
            .findAll()
            .map {
                it.toDto()
            }
            .toSet()
    }

    @Transactional
    override fun getWarehouseById(id: Long): WarehouseDto {
        return warehouseRepository
            .findById(id)
            .get()
            .toDto()
    }

    @Transactional
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

    @Transactional
    override fun updateWarehouse(id: Long, warehouseDto: PartialWarehouseDto) {
        var warehouse = warehouseRepository.findById(id).get()

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

    @Transactional
    override fun deleteWarehouse(id: Long) {
        warehouseRepository
            .deleteById(id)
    }

}