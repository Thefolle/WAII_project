package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.entities.Warehouse
import it.polito.waii.warehouse_service.repositories.WarehouseRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
@Transactional
class AlarmServiceImpl(val warehouseRepository: WarehouseRepository,val mailService: MailService) {

fun getAlarmLevel(warehouseName: String, product:String): Int{
    val warehouse= warehouseRepository.findByWarehousename(warehouseName)
    return warehouse.alarmLevel.getValue(product)
}
    fun updateAlarmLevel(newAlarm: Int, warehousename: String, product: String)
    {
        val warehouse= warehouseRepository.findByWarehousename(warehousename)
        warehouse.updateAlarm(product,newAlarm)

    }

}