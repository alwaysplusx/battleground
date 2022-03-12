package com.battleground.reactor.service

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait

@Service
class UserCoroutineService(private val databaseClient: DatabaseClient) {

    suspend fun info(request: ServerRequest): ServerResponse {
        val row = databaseClient.sql("select * from user where username=:username")
            .bind("username", request.pathVariable("username"))
            .fetch().one()
            .awaitFirstOrNull()
        if (row == null) {
            return ServerResponse.notFound().buildAndAwait()
        }
        return ServerResponse.ok().bodyValueAndAwait(row)
    }

    suspend fun register(request: ServerRequest): ServerResponse {
        val username = request.queryParam("username").orElseThrow()
        val password = request.queryParam("password").orElseThrow()
        val row = databaseClient.sql("select count(1) as count from user where username=:username")
            .bind("username", username)
            .fetch().one()
            .awaitFirstOrNull()

        if (row!!["count"].toString().toInt() > 0) {
            return ServerResponse.badRequest().bodyValueAndAwait("username exists")
        }

        val rowsUpdated = databaseClient.sql("insert into user(username, password) values(:username, :password)")
            .bind("username", username)
            .bind("password", password)
            .fetch()
            .rowsUpdated()
            .awaitSingle()

        return if (rowsUpdated == 1)
            ServerResponse.ok().bodyValueAndAwait("register succeed!")
        else
            ServerResponse.badRequest().bodyValueAndAwait("register failed!")

    }

}
