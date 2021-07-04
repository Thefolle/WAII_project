package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.entities.Warehouse
import it.polito.waii.warehouse_service.repositories.WarehouseRepository
import org.springframework.stereotype.Service
import java.util.*
@Service
class WarehouseServiceImpl (val warehouse: WarehouseRepository) {
    fun listProducts(warehouse: WarehouseRepository) : Set<String>{
        return warehouse.getProductList()
    }
    fun loadProduct(productName:String, quantity:Int, warehouseID : Warehouse) : Warehouse {

        if (warehouseID != null) {
            val products: Set<String>
            var initialQuantity: Int
            var warehouse1: Warehouse
            initialQuantity = 0
            products = warehouse.findByName(warehouseID.warehousename).products
            if (!products.contains(productName))
                warehouse.findByName(warehouseID.warehousename).products.plus(productName)
            if (warehouse.findByName(warehouseID.warehousename).productQuantity.containsKey(productName))
                initialQuantity =
                    warehouse.findByName(warehouseID.warehousename).productQuantity.getValue(productName) + quantity
            else
                initialQuantity = quantity
            warehouse1 = warehouse.findByName(warehouseID.warehousename)
            warehouse1.productQuantity.plus(Pair(productName, initialQuantity))
            warehouse.save(warehouse1)

        }
        return warehouseID
    }
    fun unloadProduct(productName:String, quantity:Int, warehouseID : Warehouse?) : Warehouse?
    {
        var warehouse1: Warehouse?
        if (warehouseID != null) {
            warehouse1=warehouse.findByName(warehouseID.warehousename)
        }else {
            warehouse1 =
                warehouse.findAll().filter { p -> p.productQuantity.get(productName)!! > 0 }.collectList().block()
                    ?.get(0)
         return warehouse1
        }
        warehouse1.unloadProduct(productName)
        warehouse.save(warehouse1)
        return warehouseID
    }

}

