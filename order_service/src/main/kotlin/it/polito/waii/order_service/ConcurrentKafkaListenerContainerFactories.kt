package it.polito.waii.order_service

import it.polito.waii.order_service.dtos.OrderDto
import org.apache.kafka.common.errors.SerializationException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.SeekToCurrentBatchErrorHandler
import org.springframework.retry.support.RetryTemplate
import org.springframework.retry.support.RetryTemplateBuilder

@Configuration
class ConcurrentKafkaListenerContainerFactories {

    // Used by createOrder consumer
    @Bean
    fun createOrderConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, OrderDto>, @Qualifier("longKafkaTemplate") replyTemplate: KafkaTemplate<String, Long>): ConcurrentKafkaListenerContainerFactory<String, OrderDto> {
        var container = ConcurrentKafkaListenerContainerFactory<String, OrderDto>()
        container.consumerFactory = consumerFactory
        container.setReplyTemplate(replyTemplate)

        return container
    }

    // Used by createOrder producer
    @Bean
    fun longConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Long>): ConcurrentKafkaListenerContainerFactory<String, Long> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Long>()
        container.consumerFactory = consumerFactory

        return container
    }

    // Used by getOrders consumer
    @Bean
    fun getOrdersConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Void>, kafkaTemplate: KafkaTemplate<String, Set<OrderDto>>): ConcurrentKafkaListenerContainerFactory<String, Void> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Void>()
        container.consumerFactory = consumerFactory
        container.setReplyTemplate(kafkaTemplate)

        return container
    }

    // Used by getOrders producer
    @Bean
    fun setOrderDtoConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Set<OrderDto>>): ConcurrentKafkaListenerContainerFactory<String, Set<OrderDto>> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Set<OrderDto>>()
        container.consumerFactory = consumerFactory

        return container
    }

    // Used by getOrder consumer
    @Bean
    fun getOrderByIdConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Long>, @Qualifier("orderDtoKafkaTemplate") replyTemplate: KafkaTemplate<String, OrderDto>): ConcurrentKafkaListenerContainerFactory<String, Long> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Long>()
        container.consumerFactory = consumerFactory
        container.setReplyTemplate(replyTemplate)

        return container
    }

    // Used by getOrder producer
    @Bean
    fun orderDtoConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, OrderDto>): ConcurrentKafkaListenerContainerFactory<String, OrderDto> {
        var container = ConcurrentKafkaListenerContainerFactory<String, OrderDto>()
        container.consumerFactory = consumerFactory

        return container
    }
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