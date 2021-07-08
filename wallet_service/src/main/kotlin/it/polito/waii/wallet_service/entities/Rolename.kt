package it.polito.waii.wallet_service.entities

enum class Rolename {
    CUSTOMER,
    ADMIN;

    override fun toString(): String {
        return "ROLE_${this.name}"
    }
}
