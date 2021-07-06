package it.polito.waii.order_service

import org.apache.kafka.common.serialization.Deserializer
import org.springframework.stereotype.Component

@Component
class DeserializationExceptionHandler: Deserializer<Long?> {
    override fun deserialize(topic: String?, data: ByteArray?): Long? {
        println("Cannot deserialize payload, it is not long-typed")
        if (data == null) return null
        if (data.size != Long.SIZE_BYTES) {
            println("Cannot deserialize payload, it is not long-typed")
        }
        return null
    }

}
