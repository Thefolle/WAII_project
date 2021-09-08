package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.dtos.*
import it.polito.waii.warehouse_service.entities.Comment
import it.polito.waii.warehouse_service.entities.Product
import it.polito.waii.warehouse_service.kafka.producer.HasProductBeenBoughtByCustomer
import it.polito.waii.warehouse_service.repositories.CommentRepository
import it.polito.waii.warehouse_service.repositories.ProductRepository
import it.polito.waii.warehouse_service.repositories.ProductWarehouseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.nio.ByteBuffer
import java.time.LocalDateTime

@Service
@Transactional
class ProductServiceImpl(
    val productRepository: ProductRepository,
    val productWarehouseRepository: ProductWarehouseRepository,
    val commentRepository: CommentRepository
) : ProductService {

    @Autowired
    lateinit var hasProductBeenBoughtByCustomerReplyingKafkaTemplate: ReplyingKafkaTemplate<String, Long, Boolean>

    override fun getProducts(): List<ProductDTO> {
        return productRepository.findAll().map { it.toDTO() }
    }

    override fun getProductsPerCategory(category: String): List<ProductDTO> {
        return productRepository.findByCategory(category).map { it.toDTO() }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun addProduct(product: ProductDTO): ProductDTO {
        val newProduct = Product(
            id = null,
            name = product.name,
            description = product.description,
            category = product.category,
            creationDate = LocalDateTime.now(),
            price = product.price,
            avgRating = 0F,
            pictureURI = product.pictureURI
        )
        productRepository.save(newProduct)
        return newProduct.toDTO()
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun updateProduct(productId: Long, product: ProductDTO): ProductDTO {
        val productOptional = productRepository.findById(productId)
        return if (productOptional.isEmpty) {
            addProduct(product)
        } else {
            val newProduct = productOptional.get()
            newProduct.name = product.name
            newProduct.description = product.description
            newProduct.category = product.category
            newProduct.price = product.price
            newProduct.pictureURI = product.pictureURI
            newProduct.toDTO()
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun patchProduct(productId: Long, product: PatchProductDTO): ProductDTO {
        val productOptional = productRepository.findById(productId)
        if (productOptional.isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No product with id $productId exists."
        )

        val newProduct = productOptional.get()
        newProduct.name = product.name ?: newProduct.name
        newProduct.description = product.description ?: newProduct.description
        newProduct.category = product.category ?: newProduct.category
        newProduct.price = product.price ?: newProduct.price
        newProduct.pictureURI = product.pictureURI ?: newProduct.pictureURI

        return newProduct.toDTO()
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun deleteProduct(productId: Long) {
        if (!productRepository.existsById(productId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No product with id $productId exists."
            )
        } else if (productWarehouseRepository.existsByProductId(productId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "The product cannot be deleted because it is still stored in some warehouse."
            )
        }

        productRepository.deleteById(productId)
    }

    override fun getProductPicture(productId: Long): String {
        val productOptional = productRepository.findById(productId)
        if (productOptional.isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No product with id $productId exists."
        )

        if (productOptional.get().toDTO().pictureURI == null) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No picture URL for product with id $productId."
        )

        return productOptional.get().toDTO().pictureURI!!
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun updateProductPicture(productId: Long, pictureURI: String) {
        val productOptional = productRepository.findById(productId)
        if (productOptional.isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No product with id $productId exists."
        )
        productOptional.get().pictureURI = pictureURI
    }

    override fun getWarehouses(productId: Long): List<WarehouseDto> {
        if (!productRepository.existsById(productId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No product with id $productId exists."
            )
        }

        return productWarehouseRepository
            .getAllByProduct(productRepository.findById(productId).get())
            .map {
                it.warehouse.toDto()
            }
    }

    private fun getProductById(productId: Long): Product {
        val eventualProduct = productRepository.findByIdOrNull(productId)
        if (eventualProduct != null) {
            return eventualProduct
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No product with id $productId exists.")
        }
    }

    override fun addComment(productId: Long, comment: CommentDTO): Long {
        if (!productRepository.existsById(productId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No product with id $productId exists.")
        } else if (!hasProductBeenBoughtByCustomer(getUsername(), productId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot comment a product that you have never bought.")
        }

        // save the comment and update the average rating of the product
        val product = getProductById(productId)

        val newCommentId =
            commentRepository.save(
                Comment(
                    null,
                    comment.title,
                    comment.body,
                    comment.stars,
                    LocalDateTime.now(),
                    product
                )
            )
            .id!!

        updateProductAverageRating(productId)

        return newCommentId
    }

    override fun getComments(productId: Long): List<CommentDTO> {
        if (!productRepository.existsById(productId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No product with id $productId exists.")
        }

        return commentRepository.findAll().map { it.toDTO() }
    }

    override fun deleteComment(productId: Long, commentId: Long) {
        if (!productRepository.existsById(productId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No product with id $productId exists.")
        }

        val eventualComment = commentRepository.findByIdOrNull(commentId)
        if (eventualComment == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No comment with id $commentId exists.")
        } else if (eventualComment.product.id != productId) {
            throw  ResponseStatusException(HttpStatus.CONFLICT, "The comment with id $commentId exists, but it is not" +
                    " attached to the product with id $productId")
        }

        commentRepository.deleteById(commentId)

        updateProductAverageRating(productId)
    }

    override fun updateComment(productId: Long, commentId: Long, comment: UpdateCommentDto) {
        if (!productRepository.existsById(productId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No product with id $productId exists.")
        }

        val eventualComment = commentRepository.findByIdOrNull(commentId)
        if (eventualComment == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No comment with id $commentId exists.")
        } else if (eventualComment.product.id != productId) {
            throw  ResponseStatusException(HttpStatus.CONFLICT, "The comment with id $commentId exists, but it is not" +
                    " attached to the product with id $productId")
        } else {
            eventualComment.title = comment.title
            eventualComment.body = comment.body
            eventualComment.stars = comment.stars
        }

        updateProductAverageRating(productId)
    }

    private fun hasProductBeenBoughtByCustomer(username: String, productId: Long): Boolean {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(12)

        val future =
            hasProductBeenBoughtByCustomerReplyingKafkaTemplate
                .sendAndReceive(
                    MessageBuilder
                        .withPayload(
                            productId
                        )
                        .setHeader(KafkaHeaders.TOPIC, "order_service_requests")
                        .setHeader(KafkaHeaders.PARTITION_ID, 5)
                        .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                        .setHeader(KafkaHeaders.REPLY_TOPIC, "order_service_responses")
                        .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                        .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                        .setHeader("username", username)
                        .build(),
                    ParameterizedTypeReference.forType<Boolean>(Boolean::class.java)
                )

        val response: Message<Boolean>
        try {
            response = future
                .get()
        } catch (exception: Exception) {
            throw ResponseStatusException(
                HttpStatus.REQUEST_TIMEOUT,
                "Purchase verification couldn't be completed due to some malfunction. Please, try later."
            )
        }

        if ((response.headers["hasException"] as Boolean)) {
            throw ResponseStatusException(
                HttpStatus.valueOf(response.headers["exceptionRawStatus"] as Int),
                response.headers["exceptionMessage"] as String
            )
        }

        return response.payload
    }

    private fun getUsername(): String {
        val principal = SecurityContextHolder.getContext().authentication.principal
        return if (principal is UserDTO) {
            principal.username
        } else {
            principal.toString()
        }
    }

    private fun updateProductAverageRating(productId: Long) {
        val product = getProductById(productId)

        val comments =
            commentRepository
                .findAllByProductId(productId)

        val newAverage =
            comments
                .map { it.stars }
                .average()

        product.avgRating = newAverage.toFloat()
    }

}
