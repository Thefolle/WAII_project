package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.dtos.*
import java.net.URI
import javax.xml.stream.events.Comment

interface ProductService {

    fun getProducts(): List<ProductDTO>

    fun getProductsPerCategory(category: String): List<ProductDTO>

    fun addProduct(product: ProductDTO): ProductDTO

    fun updateProduct(productId: Long, product: ProductDTO): ProductDTO

    fun patchProduct(productId: Long, product: PatchProductDTO): ProductDTO

    fun deleteProduct(productId: Long)

    fun getProductPicture(productId: Long): String

    fun updateProductPicture(productId: Long, pictureURI: String)

    fun getWarehouses(productId: Long): List<WarehouseDto>

    fun addComment(productId: Long, comment: CommentDTO): Long

    fun getComments(productId: Long): List<CommentDTO>

    fun deleteComment(productId: Long, commentId: Long)

    fun updateComment(productId: Long, commentId: Long, commentDTO: UpdateCommentDto)
}
