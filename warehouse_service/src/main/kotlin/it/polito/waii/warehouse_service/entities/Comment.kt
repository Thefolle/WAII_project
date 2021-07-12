package it.polito.waii.warehouse_service.entities

import it.polito.waii.warehouse_service.dtos.CommentDTO
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Comment(
    @Id
    @GeneratedValue
    var id: Long?,
    var title: String,
    var body: String,
    var stars: Int,
    var creationDate: LocalDateTime,
    @ManyToOne(fetch= FetchType.LAZY)
    var product: Product
) {
    fun toDTO(): CommentDTO = CommentDTO(
    id,
    title,
    body,
    stars,
    creationDate
    )
}
