package it.polito.waii.orchestrator.kafka.producer

import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.errors.SerializationException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ConsumerAwareBatchErrorHandler

@Configuration
class ConcurrentMessageListenerContainers {

    @Bean
    fun longConcurrentMessageListenerContainer(@Qualifier("checkWarehouseConcurrentKafkaListenerContainerFactory") containerFactory: ConcurrentKafkaListenerContainerFactory<String, Long>): ConcurrentMessageListenerContainer<String, Long> {
        var container = containerFactory.createContainer("warehouse_service_responses")
        container.containerProperties.setGroupId("warehouse_service_group_id_1")

        val consumerAwareBatchErrorHandler = ConsumerAwareBatchErrorHandler { thrownException, data, consumer ->
            if (thrownException is SerializationException) {
                consumer.seek(TopicPartition("warehouse_service_responses", 0), consumer.position(TopicPartition("warehouse_service_responses", 0)) + 1)
            }
        }
        container.setBatchErrorHandler(consumerAwareBatchErrorHandler)

        return container
    }

}