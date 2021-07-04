package it.polito.waii.order_service

import com.fasterxml.jackson.core.type.TypeReference
import it.polito.waii.order_service.dtos.OrderDto
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.lang.Nullable

class SetOrderDtoSerializer: JsonSerializer<Set<OrderDto>>() {

    override fun serialize(topic: String?, @Nullable data: Set<OrderDto>?): ByteArray? {
        if (data == null) return null

        return super.objectMapper.writeValueAsBytes(data)
    }

}