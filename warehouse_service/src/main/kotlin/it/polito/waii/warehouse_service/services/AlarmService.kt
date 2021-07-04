package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.entities.Warehouse
import org.springframework.stereotype.Service

@Service
interface AlarmService {
    fun updateAlarmLevel(newAlarm: Int, warehouse: String, product: String)
    fun getAlarmLevel(warehouse: String, product:String):Int
    fun soundAlarm(warehouse:String,product:String) //  send notification to admin
}