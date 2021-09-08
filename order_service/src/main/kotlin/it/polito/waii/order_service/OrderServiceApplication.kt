package it.polito.waii.order_service

import it.polito.waii.order_service.dtos.DeliveryDto
import it.polito.waii.order_service.dtos.InputOrderDto
import it.polito.waii.order_service.repositories.OrderRepository
import it.polito.waii.order_service.services.OrderService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import org.neo4j.driver.Driver
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager
import org.springframework.data.neo4j.repository.config.ReactiveNeo4jRepositoryConfigurationExtension
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.*
import javax.ws.rs.core.Application


@SpringBootApplication
@EnableEurekaClient
class OrderServiceApplication {

    // configure this bean since Spring Boot doesn't autoconfigure it automatically yet
    @Bean(ReactiveNeo4jRepositoryConfigurationExtension.DEFAULT_TRANSACTION_MANAGER_BEAN_NAME)
    fun reactiveTransactionManager(driver: Driver, reactiveDatabaseSelectionProvider: ReactiveDatabaseSelectionProvider): ReactiveNeo4jTransactionManager {
        return ReactiveNeo4jTransactionManager(driver, reactiveDatabaseSelectionProvider)
    }

    @Bean
    fun mailSender(@Value("\${spring.mail.host}") host: String,
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

//    @Bean
//    fun applicationInitialization(orderService: OrderService, orderRepository: OrderRepository) = ApplicationRunner {
//        runBlocking {
//            val orderId =
//                orderService
//                    .createOrder(
//                        InputOrderDto(
//                            2,
//                            mapOf(1L to DeliveryDto("Via Verdi, 7", 2)),
//                            mapOf(1L to 1)
//                        ),
//                        "Lucas",
//                        ""
//                    )
//
//            orderService
//                .deleteOrderById(
//                    orderId,
//                    "Lucas",
//                    ""
//                )
//
//            orderRepository
//                .deleteById(orderId)
//                .awaitSingleOrNull()
//        }
//
//    }

}

fun main(args: Array<String>) {
    runApplication<OrderServiceApplication>(*args)
}
