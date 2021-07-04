package it.polito.waii.warehouse_service.repositories


import kotlinx.coroutines.reactive.collect

class WarehuseRepositoryImpl(val warehouseRepository: WarehouseRepository) {
   suspend fun getProductList(): Set<String>{
        val product_names: Set<String> = setOf()
        warehouseRepository.findAll().collect ({
                warehouse ->   warehouse.products.forEach(
            {
                    name -> product_names.plusElement(name)
            })
        })
        return product_names
    }
}