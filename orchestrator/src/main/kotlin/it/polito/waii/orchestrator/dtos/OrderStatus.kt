package it.polito.waii.orchestrator.dtos

enum class OrderStatus {
    ISSUED,
    DELIVERING,
    DELIVERED,
    FAILED,
    CANCELED
}