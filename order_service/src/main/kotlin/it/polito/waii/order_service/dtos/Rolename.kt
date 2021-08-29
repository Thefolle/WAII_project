package it.polito.waii.order_service.dtos

enum class Rolename {
    CUSTOMER,
    ADMIN;

    override fun toString(): String {
        return "ROLE_${this.name}"
    }
}
