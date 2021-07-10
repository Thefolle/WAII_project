package it.polito.waii.order_service.kafka.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.waii.order_service.dtos.OrderDto
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.VoidDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class ConsumerFactories {

    val REQUEST_TIMEOUT_MS_CONFIG = 15000

    @Bean
    fun orderDtoConsumerFactory(): ConsumerFactory<String, OrderDto> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG to REQUEST_TIMEOUT_MS_CONFIG
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun voidConsumerFactory(): ConsumerFactory<String, Void> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to VoidDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG to REQUEST_TIMEOUT_MS_CONFIG
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun longConsumerFactory(): ConsumerFactory<String, Long> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to LongDeserializer::class.java,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG to REQUEST_TIMEOUT_MS_CONFIG
        )

        return DefaultKafkaConsumerFactory(config)
    }

    @Bean
    fun setOrderDtoConsumerFactory(): ConsumerFactory<String, Set<OrderDto>> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.GROUP_ID_CONFIG to "order_service_group_id",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            JsonDeserializer.TRUSTED_PACKAGES to "*",
            ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG to REQUEST_TIMEOUT_MS_CONFIG,
        )
        val objectMapper = ObjectMapper()
        val type = objectMapper.typeFactory.constructParametricType(Set::class.java, OrderDto::class.java)

        return DefaultKafkaConsumerFactory(config, StringDeserializer(), JsonDeserializer(type, objectMapper, false))
    }

}