package it.polito.waii.warehouse_service.dtos

import java.net.URI
import java.time.LocalDateTime

class PatchProductDTO (
    var id: Long?,
    var name: String?,
    var description: String?,
    var category: String?,
    var price: Float?,
    var pictureURI: String?
)
