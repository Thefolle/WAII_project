package it.polito.waii.warehouse_service.dtos

import java.time.LocalDateTime

class CommentDTO (
    var id: Long?,
    var title: String,
    var body: String,
    var stars: Int,
    var creationDate: LocalDateTime
)
