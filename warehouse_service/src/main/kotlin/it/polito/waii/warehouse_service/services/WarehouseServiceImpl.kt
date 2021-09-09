package it.polito.waii.warehouse_service.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.util.concurrent.ThreadFactoryBuilder
import it.polito.waii.warehouse_service.dtos.*
import it.polito.waii.warehouse_service.entities.*
import it.polito.waii.warehouse_service.exceptions.UnsatisfiableRequestException
import it.polito.waii.warehouse_service.repositories.ProductRepository
import it.polito.waii.warehouse_service.repositories.ProductWarehouseRepository
import it.polito.waii.warehouse_service.repositories.WarehouseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.requestreply.RequestReplyTypedMessageFuture
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.support.KafkaNull
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.time.Duration
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import kotlin.concurrent.thread

@Service
@Transactional
class WarehouseServiceImpl : WarehouseService {

    @Autowired
    lateinit var warehouseRepository: WarehouseRepository

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var productWarehouseRepository: ProductWarehouseRepository

    @Autowired
    lateinit var getAllAdminEmailsReplyingKafkaTemplate: ReplyingKafkaTemplate<String, Void, Set<String>>

    @Autowired
    lateinit var mailService: MailService


    private fun getProductWarehouseById(productId: Long, warehouseId: Long): ProductWarehouse{
        if (!warehouseRepository.existsById(warehouseId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No warehouse with id $warehouseId exists."
            )
        } else if (!productRepository.existsById(productId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No product with id $productId exists."
            )
        }

        val compKey = CompositeKey(productId, warehouseId)
        val productWarehouseOptional = productWarehouseRepository.findById(compKey)
        if (productWarehouseOptional.isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No product with id $productId exists inside warehouse with id $warehouseId."
        )
        return productWarehouseOptional.get()
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun createWarehouse(warehouseDto: WarehouseDto): Long {
        return warehouseRepository.save(
            Warehouse(
                null,
                warehouseDto.name,
                warehouseDto.city,
                warehouseDto.region,
                warehouseDto.capacity,
                null
            )
        )
            .id!!
    }


    override fun getWarehouses(): Set<WarehouseDto> {
        return warehouseRepository
            .findAll()
            .map {
                it.toDto()
            }
            .toSet()
    }


    override fun getWarehouseById(id: Long): WarehouseDto {
        if (!warehouseRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No warehouse with id $id exists.")
        }

        return warehouseRepository
            .findById(id)
            .get()
            .toDto()
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun updateWarehouse(id: Long, warehouseDto: WarehouseDto): Long? {
        return if (warehouseRepository.existsById(id)) {
            warehouseRepository.save(
                Warehouse(
                    id,
                    warehouseDto.name,
                    warehouseDto.city,
                    warehouseDto.region,
                    warehouseDto.capacity,
                    null
                )
            )
            null
        } else {
            warehouseRepository.save(
                Warehouse(
                    null,
                    warehouseDto.name,
                    warehouseDto.city,
                    warehouseDto.region,
                    warehouseDto.capacity,
                    null
                )
            )
                .id
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun updateWarehouse(id: Long, warehouseDto: PartialWarehouseDto) {
        if (!warehouseRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No warehouse with id $id exists.")
        }

        val warehouse = warehouseRepository.findById(id).get()

        if (warehouseDto.name != null) {
            warehouse.name = warehouseDto.name!!
        }
        if (warehouseDto.city != null) {
            warehouse.city = warehouseDto.city!!
        }
        if (warehouseDto.region != null) {
            warehouse.region = warehouseDto.region!!
        }
        if (warehouseDto.capacity != null) {
            warehouse.capacity = warehouseDto.capacity!!
        }

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun deleteWarehouse(id: Long) {
        if (!warehouseRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No warehouse with id $id exists.")
        } else if (productWarehouseRepository.existsByWarehouseId(id)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "The warehouse cannot be deleted because it still stores some products."
            )
        }

        warehouseRepository
            .deleteById(id)
    }

    override fun getProductQuantity(warehouseId: Long, productId: Long): Long {
        if (!productRepository.existsById(productId)) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No product with id $productId exists."
        ) else if (!warehouseRepository.existsById(warehouseId)) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No warehouse with id $warehouseId exists."
        ) else if (!productWarehouseRepository.existsById(CompositeKey(productId, warehouseId))) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "This warehouse doesn't store such a product."
            )
        }

        return getProductWarehouseById(productId, warehouseId).quantity
    }

    override fun getAllQuantities(warehouseId: Long): List<ProductQuantityDTO> {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No warehouse with id $warehouseId exists."
            )
        }

        return productWarehouseRepository
            .getAllByWarehouseId(warehouseId)
            .map { ProductQuantityDTO(it.product.name, it.quantity) }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun updateProductQuantity(warehouseId: Long, updateQuantityDTO: UpdateQuantityDTO): Float {

        if (!warehouseRepository.existsById(warehouseId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No warehouse with id $warehouseId exists.")
        }

        val productOptional = productRepository.findById(updateQuantityDTO.productId)
        if (productOptional.isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No product with id ${updateQuantityDTO.productId} exists."
        )
        val product = productOptional.get()

        val compKey = CompositeKey(updateQuantityDTO.productId, warehouseId)
        val productWarehouseOptional = productWarehouseRepository.findByCompositeKey(compKey)
        // if I need to add the quantity...
        if (updateQuantityDTO.action == Action.ADD){
            if (productWarehouseOptional.isEmpty){
                //...and if I don't have the relation, I create a new ProductWarehouse with the specified quantity (if product exists)
                val warehouse = warehouseRepository.findById(warehouseId).get()
                // I Chose 10 as a default alarm level (can be modified with another API)
                val newProductWarehouse = ProductWarehouse(compKey, product, warehouse, updateQuantityDTO.quantity, 10)
                productWarehouseRepository.save(newProductWarehouse)
            }
            else{
                //...if it already existed I simply add the quantity
                val productWarehouse = productWarehouseOptional.get()
                productWarehouse.quantity += updateQuantityDTO.quantity
            }
        }


        // if I need to sub the quantity...
        else{
            //...and if I don't have the relation, I just throw an exception
            if (productWarehouseOptional.isEmpty) throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No product with id ${updateQuantityDTO.productId} exists inside warehouse with id $warehouseId."
            )
            //else I simply sub the specified quantity and check the alarm level
            val productWarehouse = productWarehouseOptional.get()
            if (productWarehouse.quantity < updateQuantityDTO.quantity) throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Not enough products!"
            )

            productWarehouse.quantity -= updateQuantityDTO.quantity
            if (productWarehouse.quantity < productWarehouse.alarmLevel) {
                thread(start = true) {
                    sendAlarmLevelReachedByEmail(
                        productWarehouse.alarmLevel,
                        productWarehouse.product.name,
                        productWarehouse.product.id!!
                    )
                }
            }
            if (productWarehouse.quantity == 0L) {
                productWarehouseRepository.delete(productWarehouse)
            }
        }

         return product.price * updateQuantityDTO.quantity
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    override fun updateProductAlarmLevel(warehouseId: Long, productId: Long, newAlarmLevel: Long): ProductWarehouseDTO {
        val productWarehouse = getProductWarehouseById(productId, warehouseId)
        productWarehouse.alarmLevel = newAlarmLevel

        if (productWarehouse.quantity < productWarehouse.alarmLevel) {
            thread(start = true) {
                sendAlarmLevelReachedByEmail(
                    productWarehouse.alarmLevel,
                    productWarehouse.product.name,
                    productWarehouse.product.id!!
                )
            }
        }

        return productWarehouse.toDTO()
    }

    private fun getAllAdminEmails(): Set<String> {
        val replyPartition = ByteBuffer.allocate(Int.SIZE_BYTES)
        replyPartition.putInt(0)
        val correlationId = ByteBuffer.allocate(Int.SIZE_BYTES)
        correlationId.putInt(0)

        val objectMapper = ObjectMapper()
        val type = objectMapper.typeFactory.constructParametricType(Set::class.java, String::class.java)

        val future =
            getAllAdminEmailsReplyingKafkaTemplate
                .sendAndReceive(
                    MessageBuilder
                        .withPayload(
                            KafkaNull.INSTANCE
                        )
                        .setHeader(KafkaHeaders.TOPIC, "catalogue_service_requests")
                        .setHeader(KafkaHeaders.PARTITION_ID, 0)
                        .setHeader(KafkaHeaders.MESSAGE_KEY, "key1")
                        .setHeader(KafkaHeaders.REPLY_TOPIC, "catalogue_service_responses")
                        .setHeader(KafkaHeaders.REPLY_PARTITION, replyPartition.array())
                        .setHeader(KafkaHeaders.CORRELATION_ID, correlationId.array())
                        .build(),
                    ParameterizedTypeReference.forType<Set<String>>(type)
                )

        var response: Message<Set<String>>
        try {
            response = future.get()
        } catch (exception: Exception) {
            throw ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "The request cannot be processed due to some " +
                    "malfunction. Please, try later.")
        }

        if ((response.headers["hasException"] as Boolean)) {
            throw ResponseStatusException(
                HttpStatus.valueOf(response.headers["exceptionRawStatus"] as Int),
                response.headers["exceptionMessage"] as String
            )
        }

        return response.payload
    }

    override fun updateProductQuantities(updateQuantitiesDTO: Set<UpdateQuantityDtoKafka>): Float {
        return updateQuantitiesDTO
            .sumOf {
                updateProductQuantity(
                    it.warehouseId,
                    it.toUpdateQuantityDto()
                ).toDouble()
            }
            .toFloat()
    }

    private fun sendAlarmLevelReachedByEmail(alarmLevel: Long, productName: String, productId: Long) {
        try {
            val emails = getAllAdminEmails()
            emails.forEach {
                mailService
                    .sendMessage(
                        it,
                        "Alarm level reached",
                        "The alarm level ($alarmLevel)" +
                                " for product \"$productName\"" +
                                " with id $productId" +
                                " has been reached!"
                    )
            }
        } catch (exception: Exception) {
            throw ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "The mail service is currently unavailable." +
                    " Please, try later.", exception)
        }
    }



}
