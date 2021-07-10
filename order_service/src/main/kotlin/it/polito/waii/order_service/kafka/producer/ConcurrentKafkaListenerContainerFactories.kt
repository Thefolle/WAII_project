package it.polito.waii.order_service.kafka.producer

import it.polito.waii.order_service.dtos.OrderDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory

@Configuration
class ConcurrentKafkaListenerContainerFactories {

    @Bean
    fun longConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Long>): ConcurrentKafkaListenerContainerFactory<String, Long> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Long>()
        container.consumerFactory = consumerFactory

        return container
    }

    @Bean
    fun setOrderDtoConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Set<OrderDto>>): ConcurrentKafkaListenerContainerFactory<String, Set<OrderDto>> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Set<OrderDto>>()
        container.consumerFactory = consumerFactory

        return container
    }

    @Bean
    fun orderDtoConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, OrderDto>): ConcurrentKafkaListenerContainerFactory<String, OrderDto> {
        var container = ConcurrentKafkaListenerContainerFactory<String, OrderDto>()
        container.consumerFactory = consumerFactory

        return container
    }

    @Bean
    fun voidConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Void>): ConcurrentKafkaListenerContainerFactory<String, Void> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Void>()
        container.consumerFactory = consumerFactory

        return container
    }

}