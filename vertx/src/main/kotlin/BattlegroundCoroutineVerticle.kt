package com.battleground.vertx;

import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.sqlclient.Tuple
import kotlinx.coroutines.launch

class BattlegroundCoroutineVerticle : CoroutineVerticle() {

    companion object {
        const val PORT = 8080
    }

    override suspend fun start() {
        val router = Router.router(vertx)

        router.get("/app/info")
            .coroutineHandler { ctx -> appInfo(ctx) }

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(PORT)
            .await()
    }

    suspend fun appInfo(ctx: RoutingContext) {
        ctx.response().end(json { obj("hello" to "world") }.toString()).await()
    }

    fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
        handler { ctx ->
            launch(ctx.vertx().dispatcher()) {
                try {
                    fn(ctx)
                } catch (e: Exception) {
                    ctx.fail(e)
                }
            }
        }
    }

}
