package it.polito.waii.order_service.controllers

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController {

    @GetMapping
    fun getOrders() {

    }

    @GetMapping("/{id}")
    fun getOrderById(@PathVariable("id") id: Long) {

    }

    @PostMapping
    fun createOrder() {

    }

    @PatchMapping("/{id}")
    fun updateOrderById(@PathVariable("id") id: Long) {

    }

    @DeleteMapping("/{id}")
    fun deleteOrder(@PathVariable("id") id: Long) {

    }

}