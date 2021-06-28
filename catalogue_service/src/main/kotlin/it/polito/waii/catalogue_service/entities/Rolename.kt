package it.polito.waii.catalogue_service.entities

enum class Rolename {
    CUSTOMER,
    ADMIN;

    override fun toString(): String {
        return "ROLE_${this.name}"
    }
}
