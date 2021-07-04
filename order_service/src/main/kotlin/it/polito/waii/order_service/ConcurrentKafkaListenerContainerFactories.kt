package it.polito.waii.order_service

import it.polito.waii.order_service.dtos.OrderDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class ConcurrentKafkaListenerContainerFactories {

    @Bean
    fun createOrderConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, OrderDto>, replyTemplate: KafkaTemplate<String, Long>): ConcurrentKafkaListenerContainerFactory<String, OrderDto> {
        var container = ConcurrentKafkaListenerContainerFactory<String, OrderDto>()
        container.consumerFactory = consumerFactory
        container.setReplyTemplate(replyTemplate)

        return container
    }

    @Bean
    fun longConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Long>): ConcurrentKafkaListenerContainerFactory<String, Long> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Long>()
        container.consumerFactory = consumerFactory

        return container
    }

    //    @Bean
//    fun getOrdersConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Void>, kafkaTemplate: KafkaTemplate<String, Set<OrderDto>>): ConcurrentKafkaListenerContainerFactory<String, Void> {
//        var container = ConcurrentKafkaListenerContainerFactory<String, Void>()
//        container.consumerFactory = consumerFactory
//        container.setReplyTemplate(kafkaTemplate)
//
//        return container
//    }
//
//    @Bean
//    fun getOrderByIdConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Long>, @Qualifier("orderDtoKafkaTemplate") replyTemplate: KafkaTemplate<String, OrderDto>): ConcurrentKafkaListenerContainerFactory<String, Long> {
//        var container = ConcurrentKafkaListenerContainerFactory<String, Long>()
//        container.consumerFactory = consumerFactory
//        container.setReplyTemplate(replyTemplate)
//
//        return container
//    }
//
//    @Bean
//    fun updateOrderConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, OrderDto>, replyTemplate: KafkaTemplate<String, Void>): ConcurrentKafkaListenerContainerFactory<String, OrderDto> {
//        var container = ConcurrentKafkaListenerContainerFactory<String, OrderDto>()
//        container.consumerFactory = consumerFactory
//        container.setReplyTemplate(replyTemplate)
//
//        return container
//    }
//
//    @Bean
//    fun deleteOrderByIdConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Long>, replyTemplate: KafkaTemplate<String, Void>): ConcurrentKafkaListenerContainerFactory<String, Long> {
//        var container = ConcurrentKafkaListenerContainerFactory<String, Long>()
//        container.consumerFactory = consumerFactory
//        container.setReplyTemplate(replyTemplate)
//
//        return container
//    }

}