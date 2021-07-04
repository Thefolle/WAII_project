package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.entities.Warehouse
import it.polito.waii.warehouse_service.repositories.WarehouseRepository
import org.springframework.stereotype.Service
import java.util.*
@Service
interface WarehouseService {
    fun loadProduct(productName:String, quantity:Int, warehouseID : Warehouse) : Warehouse
    fun unloadProduct(productName:String, quantity:Int, warehouseID : Warehouse?) : Warehouse
    fun listProducts(warehouse: WarehouseRepository)
}