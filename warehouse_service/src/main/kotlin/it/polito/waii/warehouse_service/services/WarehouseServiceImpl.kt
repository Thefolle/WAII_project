package it.polito.waii.warehouse_service.services

import com.fasterxml.jackson.databind.ObjectMapper
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
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.time.Duration

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
        val compKey = CompositeKey(productId, warehouseId)
        val productWarehouseOptional = productWarehouseRepository.findById(compKey)
        if (productWarehouseOptional.isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No product with id $productId exists inside warehouse with id $warehouseId."
        )
        return productWarehouseOptional.get()
    }

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
        return warehouseRepository
            .findById(id)
            .get()
            .toDto()
    }


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


    override fun updateWarehouse(id: Long, warehouseDto: PartialWarehouseDto) {
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


    override fun deleteWarehouse(id: Long) {
        warehouseRepository
            .deleteById(id)
    }

    override fun getProductQuantity(warehouseId: Long, productId: Long): Long {
        if (productRepository.findById(productId).isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No product with id $productId exists."
        )
        if (warehouseRepository.findById(warehouseId).isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No warehouse with id $warehouseId exists."
        )

        return getProductWarehouseById(productId, warehouseId).quantity
    }

    override fun getAllQuantities(warehouseId: Long): List<ProductQuantityDTO> {
        if (warehouseRepository.findById(warehouseId).isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No warehouse with id $warehouseId exists."
        )
        return productWarehouseRepository.findAll().map { ProductQuantityDTO(it.product.name, it.quantity) }
    }

    override fun updateProductQuantity(warehouseId: Long, updateQuantityDTO: UpdateQuantityDTO): Float {

        if (warehouseRepository.findById(warehouseId).isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No warehouse with id $warehouseId exists."
        )

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
            if (productWarehouse.quantity < productWarehouse.alarmLevel){
                sendAlarmLevelReachedByEmail(
                    productWarehouse.alarmLevel,
                    productWarehouse.product.name,
                    productWarehouse.product.id!!
                )
            }
        }

         return product.price * updateQuantityDTO.quantity
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
                    Duration.ofSeconds(15),
                    ParameterizedTypeReference.forType<Set<String>>(type)
                )

        var result: Set<String>
        try {
            result = future.get().payload
        } catch (exception: Exception) {
            throw UnsatisfiableRequestException("The emails couldn't be retrieved due to some" +
                    " failure of the following service: catalogue_service")
        }

        return result
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
            println("Emails: $emails")
            emails.forEach {
                mailService
                    .sendMessage(
                        it,
                        "Alarm level reached",
                        "The alarm level of $alarmLevel" +
                                " for product $productName" +
                                " with id $productId" +
                                " has been reached."
                    )
            }
        } catch (exception: Exception) {
            println(exception.message)
        }
    }

    override fun updateProductAlarmLevel(warehouseId: Long, productId: Long, newAlarmLevel: Long): ProductWarehouseDTO {
        val productWarehouse = getProductWarehouseById(productId, warehouseId)
        productWarehouse.alarmLevel = newAlarmLevel
        if (productWarehouse.quantity < productWarehouse.alarmLevel){
            sendAlarmLevelReachedByEmail(
                productWarehouse.alarmLevel,
                productWarehouse.product.name,
                productWarehouse.product.id!!
            )
        }

        return productWarehouse.toDTO()
    }

}
