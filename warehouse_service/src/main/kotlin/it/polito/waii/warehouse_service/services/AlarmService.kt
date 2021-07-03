package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.entities.Warehouse

interface AlarmService {
    fun updateAlarmLevel(newAlarm: Int, warehouse: String, product: String)
    fun getAlarmLevel(warehouse: String, product:String):Int
}