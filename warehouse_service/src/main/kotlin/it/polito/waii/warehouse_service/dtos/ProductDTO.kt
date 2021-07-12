package it.polito.waii.warehouse_service.dtos

import java.net.URI
import java.time.LocalDateTime

class ProductDTO (
    var id: Long?,
    var name: String,
    var description: String,
    var category: String,
    var creationDate: LocalDateTime?,
    var price: Float,
    var avgRating: Float?,
    var pictureURI: String?
)
