package it.polito.waii.order_service

import it.polito.waii.order_service.dtos.OrderDto
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.VoidSerializer
import org.neo4j.driver.Driver
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager
import org.springframework.data.neo4j.repository.config.ReactiveNeo4jRepositoryConfigurationExtension
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.SimpleKafkaHeaderMapper
import org.springframework.kafka.support.converter.MessagingMessageConverter
import org.springframework.kafka.support.serializer.JsonSerializer


@SpringBootApplication
class OrderServiceApplication {

    // configure this bean since Spring Boot doesn't autoconfigure it automatically yet
    @Bean(ReactiveNeo4jRepositoryConfigurationExtension.DEFAULT_TRANSACTION_MANAGER_BEAN_NAME)
    fun reactiveTransactionManager(driver: Driver, reactiveDatabaseSelectionProvider: ReactiveDatabaseSelectionProvider): ReactiveNeo4jTransactionManager {
        return ReactiveNeo4jTransactionManager(driver, reactiveDatabaseSelectionProvider)
    }


}

fun main(args: Array<String>) {
    runApplication<OrderServiceApplication>(*args)
}
