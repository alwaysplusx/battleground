package com.battleground.vertx;

import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.jdbcclient.JDBCPool
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.sqlclient.Tuple
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

class BattlegroundCoroutineVerticle : CoroutineVerticle() {

    companion object {
        const val PORT = 8080
    }

    lateinit var jdbcPool: JDBCPool

    override suspend fun start() {

        this.jdbcPool = JDBCPool.pool(vertx, json {
            obj(
                "url" to "jdbc:mysql://vhost.com:3306/battleground",
                "user" to "root",
                "password" to "root"
            )
        })

        val router = Router.router(vertx)
        router.route("/register")
            .coroutineHandler { ctx -> handleRegister(ctx) }
        router.get("/info/:username")
            .coroutineHandler { ctx -> handleInfo(ctx) }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(PORT)
            .await()
    }

    private suspend fun handleInfo(ctx: RoutingContext) {
        val username = ctx.pathParam("username")
        val rows = jdbcPool.preparedQuery("select * from user where username=?")
            .execute(Tuple.of(username))
            .await()
        if (rows.size() == 0) {
            ctx.response().setStatusCode(404).end("username not exists").await()
        } else {
            ctx.response().end(rows.iterator().next().toJson().toString()).await()
        }
    }

    private suspend fun handleRegister(ctx: RoutingContext) {
        val request = ctx.request()
        val username = request.getParam("username")
        val password = request.getParam("password")

        val rows = jdbcPool.preparedQuery("select count(1) as count from user where username=?")
            .execute(Tuple.of(username))
            .await()

        val count = rows.iterator().next().toJson().getInteger("count")
        if (count != 0) {
            ctx.response().setStatusCode(409).end("username exists").await()
            return
        }
        val insertRows = jdbcPool.preparedQuery("insert into user(username, password) values(?, ?)")
            .execute(Tuple.of(username, password))
            .await()

        if (insertRows.rowCount() == 1) {
            ctx.response().end("register success").await()
        }
    }

    private fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler { ctx ->
            val dispatcher: CoroutineDispatcher = ctx.vertx().dispatcher()
            launch(dispatcher) {
                try {
                    fn(ctx)
                } catch (e: Exception) {
                    ctx.fail(e)
                }
            }
        }
    }

}
