package it.polito.waii.order_service

import it.polito.waii.order_service.dtos.OrderDto
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.VoidSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
class ProducerFactories {

    @Bean
    fun orderDtoProducerFactory(): ProducerFactory<String, OrderDto> {
        var config = mapOf(
            Pair(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"),
            Pair(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java),
            Pair(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer::class.java)
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun voidProducerFactory(): ProducerFactory<String, Void> {
        var config = mapOf(
            Pair(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"),
            Pair(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java),
            Pair(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VoidSerializer::class.java)
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun longProducerFactory(): ProducerFactory<String, Long> {
        var config = mapOf(
            Pair(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"),
            Pair(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java),
            Pair(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer::class.java)
        )

        return DefaultKafkaProducerFactory(config)
    }

    //    @Bean
//    fun setOrderDtoProducerFactory(): ProducerFactory<String, Set<OrderDto>> {
//        var config = mapOf(
//            Pair(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"),
//            Pair(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java),
//            Pair(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer::class.java)
//        )
//
//        return DefaultKafkaProducerFactory(config)
//    }

}