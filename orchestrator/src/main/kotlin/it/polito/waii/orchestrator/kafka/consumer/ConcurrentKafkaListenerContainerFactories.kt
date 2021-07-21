package it.polito.waii.orchestrator.kafka.consumer

import it.polito.waii.orchestrator.dtos.OrderDtoOrchestrator
import it.polito.waii.orchestrator.exceptions.UnsatisfiableRequestException
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.header.Headers
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.*
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.springframework.messaging.support.MessageBuilder
import org.springframework.util.backoff.BackOff
import org.springframework.util.backoff.BackOffExecution

@Configuration
class ConcurrentKafkaListenerContainerFactories {

    @Bean
    fun createOrderOrchestratorConcurrentKafkaListenerContainerFactory(@Qualifier("orderDtoOrchestratorConsumerFactory") consumerFactory: ConsumerFactory<String, String>, messageConverter: StringJsonMessageConverter, replyTemplate: KafkaTemplate<String, Long>, @Qualifier("exceptionKafkaTemplate") exceptionReplyTemplate: KafkaTemplate<String, Any>): ConcurrentKafkaListenerContainerFactory<String, String> {
        var container = ConcurrentKafkaListenerContainerFactory<String, String>()
        container.consumerFactory = consumerFactory
        container.setMessageConverter(messageConverter)
        container.setReplyTemplate(replyTemplate)

        // The backoff controls how many times Kafka will attempt to send the same request on a controller;
        // in this case, no additional attempts are allowed
        container.setErrorHandler(SeekToCurrentErrorHandler(DeadLetterPublishingRecoverer(exceptionReplyTemplate) { _, _ ->
            TopicPartition(
                "exceptions",
                0
            )
        }) { BackOffExecution { BackOffExecution.STOP } })

        return container
    }

    @Bean
    fun checkWarehouseConcurrentKafkaListenerContainerFactory(@Qualifier("checkWarehouseConsumerFactory") consumerFactory: ConsumerFactory<String, Long>, messageConverter: StringJsonMessageConverter, replyTemplate: KafkaTemplate<String, Long>, @Qualifier("exceptionKafkaTemplate") exceptionReplyTemplate: KafkaTemplate<String, Any>): ConcurrentKafkaListenerContainerFactory<String, Long> {
        var container = ConcurrentKafkaListenerContainerFactory<String, Long>()
        container.consumerFactory = consumerFactory
        container.setMessageConverter(messageConverter)
        container.setReplyTemplate(replyTemplate)

        // The backoff controls how many times Kafka will attempt to send the same request on a controller;
        // in this case, no additional attempts are allowed
        container.setErrorHandler(SeekToCurrentErrorHandler(DeadLetterPublishingRecoverer(exceptionReplyTemplate) { _, _ ->
            TopicPartition(
                "exceptions",
                0
            )
        }) { BackOffExecution { BackOffExecution.STOP } })

        return container
    }

}