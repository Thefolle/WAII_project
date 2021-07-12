package it.polito.waii.warehouse_service.services

import it.polito.waii.warehouse_service.dtos.PatchProductDTO
import it.polito.waii.warehouse_service.dtos.ProductDTO
import java.net.URI

interface ProductService {

    fun getProducts(): List<ProductDTO>

    fun getProductsPerCategory(category: String): List<ProductDTO>

    fun addProduct(product: ProductDTO): ProductDTO

    fun updateProduct(productId: Long, product: ProductDTO): ProductDTO

    fun patchProduct(productId: Long, product: PatchProductDTO): ProductDTO

    fun deleteProduct(productId: Long)

    fun getProductPicture(productId: Long): String

    fun updateProductPicture(productId: Long, pictureURI: String)
}
