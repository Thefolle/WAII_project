package it.polito.waii.warehouse_service.entities

enum class Rolename {
    CUSTOMER,
    ADMIN;

    override fun toString(): String {
        return "ROLE_${this.name}"
    }
}
