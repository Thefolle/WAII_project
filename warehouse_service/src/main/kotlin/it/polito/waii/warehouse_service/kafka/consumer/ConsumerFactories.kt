package it.polito.waii.warehouse_service.kafka.consumer

import it.polito.waii.warehouse_service.dtos.UpdateQuantityDtoKafka
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory

@Configuration
class ConsumerFactories {

    @Bean
    fun updateQuantityConsumerFactory(): ConsumerFactory<Any, Any> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "warehouse_service_group_id",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest"
        )

        return DefaultKafkaConsumerFactory(config)
    }

}