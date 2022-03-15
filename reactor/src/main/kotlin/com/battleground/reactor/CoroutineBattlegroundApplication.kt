package com.battleground.reactor

import com.battleground.reactor.service.UserCoroutineService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.server.coRouter

@SpringBootApplication
open class CoroutineBattlegroundApplication {

    @Bean
    open fun route(userService: UserCoroutineService) = coRouter {
        GET("/kt/info/{username}") { userService.info(it) }
        POST("/kt/register") { userService.register(it) }
    }

}

fun main(args: Array<String>) {
    runApplication<CoroutineBattlegroundApplication>(*args)
}

