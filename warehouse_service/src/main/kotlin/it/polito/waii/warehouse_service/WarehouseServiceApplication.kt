package it.polito.waii.warehouse_service


import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.VoidSerializer
import org.neo4j.cypherdsl.core.renderer.Configuration
import org.neo4j.driver.Driver
import org.springframework.beans.factory.annotation.Value
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
import org.springframework.kafka.support.KafkaNull
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.*

@SpringBootApplication
class WarehouseServiceApplication{
    @Bean
    fun applicationRunner(): ApplicationRunner {
        return ApplicationRunner {

        }
    }
    @Bean
    fun getMailSender(@Value("\${spring.mail.host}") host: String,
                      @Value("\${spring.mail.port}") port: Int,
                      @Value("\${spring.mail.username}") username: String,
                      @Value("\${spring.mail.password}") password: String,
                      @Value("\${spring.mail.protocol}") protocol: String,
                      @Value("\${spring.mail.properties.mail.smtp.auth}") auth: Boolean,
                      @Value("\${spring.mail.properties.mail.smtp.starttls.enable}") enable: Boolean,
                      @Value("\${spring.mail.properties.mail.debug}") debug: Boolean
    ): JavaMailSender {

        val javaMailSenderImpl = JavaMailSenderImpl()
        javaMailSenderImpl.host = host
        javaMailSenderImpl.port = port
        javaMailSenderImpl.username = username
        javaMailSenderImpl.password = password
        val props: Properties = javaMailSenderImpl.javaMailProperties
        props["mail.transport.protocol"] = protocol
        props["mail.smtp.auth"] = auth
        props["mail.smtp.starttls.enable"] = enable
        props["mail.debug"] = debug

        // Uncomment to test the mail connection at startup
        //javaMailSenderImpl.testConnection()

        return javaMailSenderImpl
    }
    @Bean(ReactiveNeo4jRepositoryConfigurationExtension.DEFAULT_TRANSACTION_MANAGER_BEAN_NAME)
    fun reactiveTransactionManager(driver2: Driver, reactiveDatabaseSelectionProvider: ReactiveDatabaseSelectionProvider): ReactiveNeo4jTransactionManager {
        return ReactiveNeo4jTransactionManager(driver2, reactiveDatabaseSelectionProvider)
    }
}

fun main(args: Array<String>) {
    runApplication<WarehouseServiceApplication>(*args)
}
