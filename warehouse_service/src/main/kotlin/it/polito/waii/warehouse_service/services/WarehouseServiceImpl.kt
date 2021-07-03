package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.entities.Warehouse
import it.polito.waii.warehouse_service.repositories.WarehouseRepository
import java.util.*

class WarehouseServiceImpl (val wareshoue: WarehouseRepository) {
    fun listProducts(warehouse: WarehouseRepository) : Set<String>{
        return warehouse.getProductList()
    }
    fun loadProduct(productName:String, quantity:Int, warehouseID : Warehouse) : Warehouse
    {

      if(warehouseID!=null) {
       val products: Set<String>
       var initialQuantity: Int
        initialQuantity=0
       products=wareshoue.findByName(warehouseID.warehousename).products
        if(!  products.contains(productName))
            wareshoue.findByName(warehouseID.warehousename).products.plus(productName)
          if(wareshoue.findByName(warehouseID.warehousename).productQuantity.containsKey(productName) )
          initialQuantity=wareshoue.findByName(warehouseID.warehousename).productQuantity.getValue(productName)+quantity
        else
            initialQuantity=quantity
          wareshoue.findByName(warehouseID.warehousename).productQuantity.getValue(productName)=
      }

    }
    fun unloadProduct(productName:String, quantity:Int, warehouseID : Warehouse?) : Optional<Warehouse>
    {
        val warehouseOpt: Optional<Warehouse>
    }
}

