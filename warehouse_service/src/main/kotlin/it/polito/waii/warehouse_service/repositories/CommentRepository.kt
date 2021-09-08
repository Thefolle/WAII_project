package it.polito.waii.warehouse_service.repositories

import it.polito.waii.warehouse_service.entities.Comment
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentRepository: CrudRepository<Comment, Long> {

    fun findAllByProductId(productId: Long): List<Comment>

}