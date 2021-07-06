package it.polito.waii.order_service

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.waii.order_service.dtos.OrderDto
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.VoidSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class ProducerFactories {

    @Bean
    fun orderDtoProducerFactory(): ProducerFactory<String, OrderDto> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun voidProducerFactory(): ProducerFactory<String, Void> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to VoidSerializer::class.java
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun longProducerFactory(): ProducerFactory<String, Long> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to LongSerializer::class.java,
            ProducerConfig.RETRIES_CONFIG to 1
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun setOrderDtoProducerFactory(): ProducerFactory<String, Set<OrderDto>> {
        var config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092"
        )

        val objectMapper = ObjectMapper()
        val type = objectMapper.typeFactory.constructParametricType(Set::class.java, OrderDto::class.java)

        return DefaultKafkaProducerFactory(config, StringSerializer(), JsonSerializer(type, objectMapper))
    }

}