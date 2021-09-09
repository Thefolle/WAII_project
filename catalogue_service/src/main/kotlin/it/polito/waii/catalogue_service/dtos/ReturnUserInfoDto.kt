package it.polito.waii.catalogue_service.dtos

class ReturnUserInfoDto (
    var username: String,
    var mail: String,
    var name: String,
    var surname: String,
    var deliveryAddress: String,
    var isEnabled:Boolean,
    var isAdmin:Boolean,
    var isCustomer: Boolean
)