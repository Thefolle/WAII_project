package it.polito.waii.order_service.kafka.consumer

import it.polito.waii.order_service.dtos.OrderDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaTemplates {

    @Bean
    fun orderDtoKafkaTemplate(producerFactory: ProducerFactory<String, OrderDto>): KafkaTemplate<String, OrderDto> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun voidKafkaTemplate(producerFactory: ProducerFactory<String, Void>): KafkaTemplate<String, Void> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun longKafkaTemplate(producerFactory: ProducerFactory<String, Long>): KafkaTemplate<String, Long> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun setOrderDtoKafkaTemplate(producerFactory: ProducerFactory<String, Set<OrderDto>>): KafkaTemplate<String, Set<OrderDto>> {
        return KafkaTemplate(producerFactory)
    }

}