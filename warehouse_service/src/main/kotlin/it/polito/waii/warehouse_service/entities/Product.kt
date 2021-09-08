package it.polito.waii.warehouse_service.entities


import it.polito.waii.warehouse_service.dtos.ProductDTO
import java.net.URI
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class Product(
    @Id
    @GeneratedValue
    var id: Long?,
    var name: String,
    var description: String,
    var category: String,
    var creationDate: LocalDateTime,
    var price: Float,
    var avgRating: Float,
    @OneToMany(mappedBy = "product")
    var comments : MutableSet<Comment>? = mutableSetOf(),

    var pictureURI: String?,


    @OneToMany(mappedBy = "warehouse")
    var warehouses: MutableSet<ProductWarehouse>? = mutableSetOf()
) {
    fun toDTO(): ProductDTO = ProductDTO(
        id,
        name,
        description,
        category,
        creationDate,
        price,
        avgRating,
        pictureURI
    )
}
