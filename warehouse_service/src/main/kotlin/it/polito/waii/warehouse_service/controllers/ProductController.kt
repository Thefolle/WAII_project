package it.polito.waii.warehouse_service.controllers

import it.polito.waii.warehouse_service.dtos.PatchProductDTO
import it.polito.waii.warehouse_service.dtos.ProductDTO
import it.polito.waii.warehouse_service.dtos.WarehouseDto
import it.polito.waii.warehouse_service.services.ProductServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class ProductController(val productServiceImpl: ProductServiceImpl) {

    @GetMapping("/")
    fun getProductsPerCategory(@RequestParam("category", required = false) category: String?): ResponseEntity<List<ProductDTO>> {
        return if (category == null) ResponseEntity.status(HttpStatus.OK).body(productServiceImpl.getProducts())
        else ResponseEntity.status(HttpStatus.OK).body(productServiceImpl.getProductsPerCategory(category))
    }

    @PostMapping("/")
    fun addProduct(@RequestBody product: ProductDTO): ResponseEntity<ProductDTO>{
        return ResponseEntity.status(HttpStatus.CREATED).body(productServiceImpl.addProduct(product))
    }

    @PutMapping("/{productId}")
    fun updateProduct(@PathVariable("productId") productId: Long, @RequestBody product: ProductDTO): ResponseEntity<ProductDTO>{
        return ResponseEntity.status(HttpStatus.OK).body(productServiceImpl.updateProduct(productId, product))
    }

    @PatchMapping("/{productId}")
    fun patchProduct(@PathVariable("productId") productId: Long, @RequestBody product: PatchProductDTO): ResponseEntity<ProductDTO>{
        return ResponseEntity.status(HttpStatus.OK).body(productServiceImpl.patchProduct(productId, product))
    }

    @DeleteMapping("/{productId}")
    fun deleteProduct(@PathVariable("productId") productId: Long): ResponseEntity<String>{
        productServiceImpl.deleteProduct(productId)
        return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully!")
    }

    @GetMapping("/{productId}/picture")
    fun getProductPicture(@PathVariable("productId") productId: Long): ResponseEntity<String>{
        return ResponseEntity.status(HttpStatus.OK).body(productServiceImpl.getProductPicture(productId))
    }

    @PostMapping("/{productId}/picture")
    fun updateProductPicture(@PathVariable("productId") productId: Long, @RequestBody pictureURI: String): ResponseEntity<String>{
        productServiceImpl.updateProductPicture(productId, pictureURI)
        return ResponseEntity.status(HttpStatus.OK).body("Product picture updated successfully!")
    }

    @GetMapping("/{productId}/warehouses")
    fun getWarehouses(@PathVariable("productId") productId: Long) : ResponseEntity<List<WarehouseDto>> {
        return ResponseEntity.status(HttpStatus.OK).body(productServiceImpl.getWarehouses(productId))
    }

}
