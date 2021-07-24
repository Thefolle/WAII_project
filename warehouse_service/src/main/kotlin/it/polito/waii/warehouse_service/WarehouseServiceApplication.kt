package it.polito.waii.warehouse_service

import it.polito.waii.warehouse_service.dtos.ProductDTO
import it.polito.waii.warehouse_service.dtos.UpdateQuantityDTO
import it.polito.waii.warehouse_service.dtos.WarehouseDto
import it.polito.waii.warehouse_service.entities.Action
import it.polito.waii.warehouse_service.services.ProductService
import it.polito.waii.warehouse_service.services.WarehouseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import java.time.LocalDateTime
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
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
                    4,
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

}

fun main(args: Array<String>) {
    runApplication<WarehouseServiceApplication>(*args)
}
