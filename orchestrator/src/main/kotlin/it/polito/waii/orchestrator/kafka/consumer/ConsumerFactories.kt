package it.polito.waii.orchestrator.kafka.consumer

import it.polito.waii.orchestrator.dtos.OrderDtoOrchestrator
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
class ConsumerFactories {

    val REQUEST_TIMEOUT_MS_CONFIG = 15000

    @Bean
    fun orderDtoOrchestratorConsumerFactory(): ConsumerFactory<String, String> {
        var config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.GROUP_ID_CONFIG to "orchestrator_group_id",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "latest",
            ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG to REQUEST_TIMEOUT_MS_CONFIG
        )

        return DefaultKafkaConsumerFactory(config)
    }

}