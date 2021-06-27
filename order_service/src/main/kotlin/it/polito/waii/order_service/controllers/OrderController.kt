package it.polito.waii.order_service.controllers

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController {

    @GetMapping
    fun getOrders() {

    }

    @GetMapping
    fun getOrderById(id: Long) {

    }

    @PostMapping
    fun createOrder() {

    }

    @PatchMapping
    fun updateOrderById(id: Long) {

    }

    @DeleteMapping
    fun deleteOrder(id: Long) {

    }

}