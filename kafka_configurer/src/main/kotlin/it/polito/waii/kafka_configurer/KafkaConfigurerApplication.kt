package it.polito.waii.kafka_configurer

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class KafkaConfigurerApplication {

    @Bean
    fun fakeTopic(): NewTopic {
        return NewTopic("fakeTopic", 1, 1)
    }

}

fun main(args: Array<String>) {
    runApplication<KafkaConfigurerApplication>(*args)
}
