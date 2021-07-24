package it.polito.waii.wallet_service

import it.polito.waii.wallet_service.entities.Recharge
import it.polito.waii.wallet_service.entities.Transaction
import it.polito.waii.wallet_service.entities.Wallet
import it.polito.waii.wallet_service.repositories.RechargeRepository
import it.polito.waii.wallet_service.repositories.TransactionRepository
import it.polito.waii.wallet_service.repositories.WalletRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@SpringBootApplication
@EnableEurekaClient
//@EnableAspectJAutoProxy(proxyTargetClass = true)
class WalletServiceApplication{

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
    fun populateDB (walletRepository: WalletRepository,
                    rechargeRepository: RechargeRepository,
                    transactionRepository: TransactionRepository): ApplicationRunner{
        return ApplicationRunner {
            // Create wallets
            // User Sofia has 2 wallets
            val w1 = Wallet(null, "Sofia",50F)
            walletRepository.save(w1)
            val w2 = Wallet(null, "Sofia", 17F)
            walletRepository.save(w2)
            val w3 = Wallet(null, "Luca", 31F)
            walletRepository.save(w3)

            // Make transactions
            // do charge
            val t1 = Transaction(null, w1, 3F, LocalDateTime.now(), false, 1, null)
            transactionRepository.save(t1)
            // do recharge
            val r1 = Recharge(null, 5F, LocalDateTime.now())
            rechargeRepository.save(r1)
            val t2 = Transaction(null, w3, 5F, LocalDateTime.now(), true, 2, r1)
            transactionRepository.save(t2)

        }
    }

}

fun main(args: Array<String>) {
    runApplication<WalletServiceApplication>(*args)
}
