package it.polito.waii.warehouse_service

import it.polito.waii.warehouse_service.dtos.ProductDTO
import it.polito.waii.warehouse_service.dtos.UpdateQuantityDTO
import it.polito.waii.warehouse_service.dtos.WarehouseDto
import it.polito.waii.warehouse_service.entities.*
import it.polito.waii.warehouse_service.repositories.ProductRepository
import it.polito.waii.warehouse_service.repositories.ProductWarehouseRepository
import it.polito.waii.warehouse_service.repositories.WarehouseRepository
import it.polito.waii.warehouse_service.services.ProductService
import it.polito.waii.warehouse_service.services.WarehouseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.time.LocalDateTime
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@SpringBootApplication
@EnableEurekaClient
class WarehouseServiceApplication {

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var warehouseService: WarehouseService

    @Bean
    fun runner(): ApplicationRunner = ApplicationRunner {
        val productId = productService
            .addProduct(
                ProductDTO(
                    null,
                    "pumpkin seeds",
                    "Tasty",
                    "dry fruit",
                    LocalDateTime.now(),
                    1.5f,
                    null,
                    "www.shutterstock.com/dry_fruits/seeds"
                )
            )
            .id!!

        val warehouseId = warehouseService
            .createWarehouse(
                WarehouseDto(
                    null,
                    "super warehouse",
                    "Reggio Calabria",
                    "Calabria",
                    100,
                    null
                )
            )

        warehouseService
            .updateProductQuantity(
                warehouseId,
                UpdateQuantityDTO(
                    productId,
                    15,
                    Action.ADD
                )
            )
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Bean
    fun getAuthenticationEntryPoint(): AuthenticationEntryPoint {
        return AuthenticationEntryPoint() { httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, authenticationException: AuthenticationException ->
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        }
    }

    @Bean
    fun populateDB (warehouseRepository: WarehouseRepository,
                    productRepository: ProductRepository,
                    productWarehouseRepository: ProductWarehouseRepository): ApplicationRunner{
        return ApplicationRunner {
            // Create warehouse
            val w1 = Warehouse(null, "Food","Firenze", "Toscana",
                                100, null)
            warehouseRepository.save(w1)

            val w2 = Warehouse(null, "Vehicle","Pisa", "Toscana",
                100, null)
            warehouseRepository.save(w2)

            val w3 = Warehouse(null, "Vino","Alba", "Piemonte",
                200, null)
            warehouseRepository.save(w3)

            val w4 = Warehouse(null, "Water","Torino", "Piemonte",
                150, null)
            warehouseRepository.save(w4)

            // Add products
            var p1 = Product(null, "melon", "origin Sicily", "fruit",
                                LocalDateTime.now(), 3F, 2.1F,
                                null,null, null)
            productRepository.save(p1)

            val p2 = Product(null, "apple", "origin Sicily", "fruit",
                                LocalDateTime.now(), 2F, 3.5F,
                                null,null, null)
            productRepository.save(p2)

            // make comment
//            val c1 = mutableSetOf<Comment>(Comment(null, "About Melon",
//                                                        "Juicy", 4,
//                                                    LocalDateTime.now(), p1))
//
//            p1.comments = c1
//            productRepository.save(p1)

            var p3 = Product(null, "lorry", "capacity 10 tons", "truck",
                LocalDateTime.now(), 30F, 3.1F,
                null,null, null)
            productRepository.save(p3)

            val p4 = Product(null, "water naturale", "origin Piemonte", "drink",
                LocalDateTime.now(), 1F, 4.5F,
                null,null, null)
            productRepository.save(p4)


            // link products and warehouse
            val ckey1 = CompositeKey(p1.id!!, w1.id!!)
            val pw1 = ProductWarehouse(ckey1, p1, w1, 30, 3)
            productWarehouseRepository.save(pw1)

            val ckey2 = CompositeKey(p2.id!!, w1.id!!)
            val pw2 = ProductWarehouse(ckey2, p2, w1, 35, 7)
            productWarehouseRepository.save(pw2)

            val ckey3 = CompositeKey(p3.id!!, w3.id!!)
            val pw3 = ProductWarehouse(ckey3, p3, w1, 50, 13)
            productWarehouseRepository.save(pw3)

            val ckey4 = CompositeKey(p4.id!!, w4.id!!)
            val pw4 = ProductWarehouse(ckey4, p4, w4, 21, 8)
            productWarehouseRepository.save(pw4)

        }
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

}

fun main(args: Array<String>) {
    runApplication<WarehouseServiceApplication>(*args)
}
