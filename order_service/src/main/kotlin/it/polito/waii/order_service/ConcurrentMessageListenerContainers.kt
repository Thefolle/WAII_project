package it.polito.waii.order_service

import it.polito.waii.order_service.dtos.OrderDto
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.SerializationException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.*
import org.springframework.kafka.retrytopic.DeadLetterPublishingRecovererFactory
import org.springframework.util.backoff.FixedBackOff

/**
 * This class is used by producers only to listen to the response
 */

@Configuration
class ConcurrentMessageListenerContainers {

    // containers must have a different group id for some reason;
    // thus, non-interested containers must neglect the message through the error handler

    @Bean
    fun longConcurrentMessageListenerContainer(@Qualifier("longConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Long>): ConcurrentMessageListenerContainer<String, Long> {
        var container = containerFactory.createContainer("order_service_responses")
        container.containerProperties.setGroupId("outer_service_group_id")

        // this error handler is called when a listener receives a message from the shared reply topic that
        // is directed to another handler; the topic is just discarded by passing to the next message
        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("order_service_responses", 0), consumer.position(TopicPartition("order_service_responses", 0)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)


        return container
    }

    @Bean
    fun setOrderDtoConcurrentMessageListenerContainer(@Qualifier("setOrderDtoConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Set<OrderDto>>): ConcurrentMessageListenerContainer<String, Set<OrderDto>> {
        var container = containerFactory.createContainer("order_service_responses")
        container.containerProperties.setGroupId("outer_service_group_id_2")

        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("order_service_responses", 0), consumer.position(TopicPartition("order_service_responses", 0)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)

        return container
    }

    @Bean
    fun orderDtoConcurrentMessageListenerContainer(@Qualifier("orderDtoConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, OrderDto>): ConcurrentMessageListenerContainer<String, OrderDto> {
        var container = containerFactory.createContainer("order_service_responses")
        container.containerProperties.setGroupId("order_service_group_id_3")

        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("order_service_responses", 0), consumer.position(TopicPartition("order_service_responses", 0)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)

        return container
    }

}