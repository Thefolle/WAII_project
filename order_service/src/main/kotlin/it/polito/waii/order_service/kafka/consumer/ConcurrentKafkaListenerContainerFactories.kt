package it.polito.waii.order_service.kafka.consumer

import it.polito.waii.order_service.dtos.OrderDto
import it.polito.waii.order_service.dtos.PatchOrderDto
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

@Configuration("consumerConcurrentKafkaListenerContainerFactories")
class ConcurrentKafkaListenerContainerFactories {

    @Bean
    fun createOrderConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, OrderDto>, @Qualifier("longKafkaTemplate") replyTemplate: KafkaTemplate<String, Long>): ConcurrentKafkaListenerContainerFactory<String, OrderDto> {
        var container = ConcurrentKafkaListenerContainerFactory<String, OrderDto>()
        container.consumerFactory = consumerFactory
        container.setReplyTemplate(replyTemplate)

        return container
    }

    @Bean
    fun getOrdersConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Void>, kafkaTemplate: KafkaTemplate<String, Set<OrderDto>>): ConcurrentKafkaListenerContainerFactory<String, Void> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Void>()
        container.consumerFactory = consumerFactory
        container.setReplyTemplate(kafkaTemplate)

        return container
    }

    @Bean
    fun getOrderByIdConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Long>, @Qualifier("orderDtoKafkaTemplate") replyTemplate: KafkaTemplate<String, OrderDto>): ConcurrentKafkaListenerContainerFactory<String, Long> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Long>()
        container.consumerFactory = consumerFactory
        container.setReplyTemplate(replyTemplate)

        return container
    }

    @Bean
    fun updateOrderConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, PatchOrderDto>, @Qualifier("voidKafkaTemplate") replyTemplate: KafkaTemplate<String, Void>): ConcurrentKafkaListenerContainerFactory<String, PatchOrderDto> {
        var container = ConcurrentKafkaListenerContainerFactory<String, PatchOrderDto>()
        container.consumerFactory = consumerFactory
        container.setReplyTemplate(replyTemplate)

        return container
    }

    @Bean
    fun deleteOrderByIdConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, Long>, @Qualifier("voidKafkaTemplate") replyTemplate: KafkaTemplate<String, Void>): ConcurrentKafkaListenerContainerFactory<String, Long> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Long>()
        container.consumerFactory = consumerFactory
        container.setReplyTemplate(replyTemplate)

        return container
    }

    @Bean
    fun exceptionsConcurrentKafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, String>): ConcurrentKafkaListenerContainerFactory<String, String> {
        var container = ConcurrentKafkaListenerContainerFactory<String, String>()
        container.consumerFactory = consumerFactory

        return container
    }

}