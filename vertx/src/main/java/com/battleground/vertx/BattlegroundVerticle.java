package com.battleground.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BattlegroundVerticle extends AbstractVerticle {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(BattlegroundVerticle.class.getName(), asyncResult -> {
            log.info("deploy verticle, succeeded={}", asyncResult.succeeded());
        });
    }

    private JDBCPool jdbcPool;

    @Override
    public void start() throws Exception {
        JDBCConnectOptions connectOptions = new JDBCConnectOptions()
            .setJdbcUrl("jdbc:mysql://vhost.com:3306/battleground")
            .setUser("root")
            .setPassword("root");

        this.jdbcPool = JDBCPool.pool(vertx, connectOptions, new PoolOptions());

        Router router = Router.router(vertx);
        router.route("/register")
            .handler(this::handleRegister);

        router.get("/info/:username")
            .handler(this::handleInfo);

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(PORT, asyncResult -> {
                log.info("start verticle at port {}, succeeded={}", PORT, asyncResult.succeeded());
            });

    }

    public void handleRegister(RoutingContext ctx) {
        HttpServerRequest request = ctx.request();
        String username = request.getParam("username");
        String password = request.getParam("password");
        jdbcPool.preparedQuery("select count(1) as count from user where username=?")
            .execute(Tuple.of(username))
            .onSuccess(rowSet -> {
                Integer count = rowSet.iterator().next().toJson().getInteger("count");
                if (count != 0) {
                    ctx.response().setStatusCode(409).end("username exists");
                    return;
                }
                jdbcPool.preparedQuery("insert into user(username, password) values(?, ?)")
                    .execute(Tuple.of(username, password))
                    .onSuccess(insertRowSet -> {
                        if (insertRowSet.rowCount() == 1) {
                            ctx.response().end("register succeed");
                        } else {
                            ctx.response().setStatusCode(500).end("register failed!");
                        }
                    })
                    .onFailure(error -> {
                        ctx.response().setStatusCode(500).end("error: " + error.getMessage());
                    });
            })
            .onFailure(error -> {
                error.printStackTrace();
                ctx.response().setStatusCode(500).end("error: " + error.getMessage());
            });

    }

    public void handleInfo(RoutingContext ctx) {
        String username = ctx.pathParam("username");
        jdbcPool.preparedQuery("select * from user where username=?")
            .execute(Tuple.of(username))
            .onSuccess(new Handler<RowSet<Row>>() {
                @Override
                public void handle(RowSet<Row> event) {
                    if (event.size() == 0) {
                        ctx.response().setStatusCode(404).end("username not exists");
                        return;
                    }
                    ctx.response().end(event.iterator().next().toJson().toString());
                }
            })
            .onFailure(new Handler<Throwable>() {
                @Override
                public void handle(Throwable event) {
                    event.printStackTrace();
                    ctx.response().setStatusCode(500).end("error: " + event.getMessage());
                }
            });
    }

}
