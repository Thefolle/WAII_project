package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.dtos.PatchProductDTO
import it.polito.waii.warehouse_service.dtos.ProductDTO
import it.polito.waii.warehouse_service.entities.Product
import it.polito.waii.warehouse_service.repositories.ProductRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.time.LocalDateTime

@Service
@Transactional
class ProductServiceImpl(val productRepository: ProductRepository): ProductService {

    override fun getProducts(): List<ProductDTO> {
        return productRepository.findAll().map { it -> it.toDTO() }
    }

    override fun getProductsPerCategory(category: String): List<ProductDTO> {
        return productRepository.findByCategory(category).map { it -> it.toDTO() }
    }

    override fun addProduct(product: ProductDTO): ProductDTO {
        val newProduct = Product(
            id = null,
            name = product.name,
            description = product.description,
            category = product.category,
            creationDate = LocalDateTime.now(),
            price = product.price,
            avgRating = 0F
        )
        productRepository.save(newProduct)
        return newProduct.toDTO()
    }

    override fun updateProduct(productId: Long, product: ProductDTO): ProductDTO {
        val productOptional = productRepository.findById(productId)
        return if (productOptional.isEmpty){
            addProduct(product)
        } else{
            val newProduct = productOptional.get()
            newProduct.name = product.name
            newProduct.description = product.description
            newProduct.category = product.category
            newProduct.price = product.price
            newProduct.pictureURI = product.pictureURI
            newProduct.toDTO()
        }
    }

    override fun patchProduct(productId: Long, product: PatchProductDTO): ProductDTO {
        val productOptional = productRepository.findById(productId)
        if (productOptional.isEmpty) throw ResponseStatusException(HttpStatus.NOT_FOUND, "No product with id $productId exists.")

        val newProduct = productOptional.get()
        newProduct.name = product.name?: newProduct.name
        newProduct.description = product.description?: newProduct.description
        newProduct.category = product.category?: newProduct.category
        newProduct.price = product.price?: newProduct.price
        newProduct.pictureURI = product.pictureURI?: newProduct.pictureURI

        return newProduct.toDTO()
    }

    override fun deleteProduct(productId: Long) {
        productRepository.deleteById(productId)
    }

    override fun getProductPicture(productId: Long): String {
        val productOptional = productRepository.findById(productId)
        if (productOptional.isEmpty) throw ResponseStatusException(HttpStatus.NOT_FOUND, "No product with id $productId exists.")

        if (productOptional.get().toDTO().pictureURI == null) throw ResponseStatusException(HttpStatus.NOT_FOUND, "No picture URL for product with id $productId.")

        return productOptional.get().toDTO().pictureURI!!
    }

    override fun updateProductPicture(productId: Long, pictureURI: String) {
        val productOptional = productRepository.findById(productId)
        if (productOptional.isEmpty) throw ResponseStatusException(HttpStatus.NOT_FOUND, "No product with id $productId exists.")
        productOptional.get().pictureURI = pictureURI
    }

}
