package it.polito.waii.kafka_configurer

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class KafkaConfigurerApplication {

    @Bean
    fun orderServiceRequestsTopic(): NewTopic {
        return NewTopic("order_service_requests", 5, 1)
    }

    @Bean
    fun orderServiceResponsesTopic(): NewTopic {
        return NewTopic("order_service_responses", 1, 1)
    }

    @Bean
    fun orchestratorRequestsTopic(): NewTopic {
        return NewTopic("orchestrator_requests", 1, 1)
    }

    @Bean
    fun orchestratorResponsesTopic(): NewTopic {
        return NewTopic("orchestrator_responses", 1, 1)
    }

}

fun main(args: Array<String>) {
    runApplication<KafkaConfigurerApplication>(*args)
}
