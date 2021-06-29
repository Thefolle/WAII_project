package it.polito.waii.order_service

import it.polito.waii.order_service.dtos.OrderDto
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.neo4j.cypherdsl.core.renderer.Configuration
import org.neo4j.driver.Driver
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager
import org.springframework.data.neo4j.repository.config.ReactiveNeo4jRepositoryConfigurationExtension
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@SpringBootApplication
class OrderServiceApplication {

    @Bean
    fun producerFactory(): ProducerFactory<String, OrderDto> {
        var config = mapOf(
            Pair(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"),
            Pair(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java),
            Pair(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer::class.java)
        )

        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, OrderDto>): KafkaTemplate<String, OrderDto> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun applicationRunner(): ApplicationRunner {
        return ApplicationRunner {

        }
    }

    // configure this bean since Spring Boot doesn't autoconfigure it automatically yet
    @Bean(ReactiveNeo4jRepositoryConfigurationExtension.DEFAULT_TRANSACTION_MANAGER_BEAN_NAME)
    fun reactiveTransactionManager(driver: Driver, reactiveDatabaseSelectionProvider: ReactiveDatabaseSelectionProvider): ReactiveNeo4jTransactionManager {
        return ReactiveNeo4jTransactionManager(driver, reactiveDatabaseSelectionProvider)
    }


}

fun main(args: Array<String>) {
    runApplication<OrderServiceApplication>(*args)
}
