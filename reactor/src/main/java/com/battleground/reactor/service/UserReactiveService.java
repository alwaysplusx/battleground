package com.battleground.reactor.service;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class UserReactiveService {

    private final DatabaseClient databaseClient;

    @NotNull
    public Mono<ServerResponse> info(ServerRequest request) {
        return databaseClient.sql("select * from user where username=:username")
            .bind("username", request.pathVariable("username"))
            .fetch().one()
            .flatMap(row -> ServerResponse.ok().bodyValue(row))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    @NotNull
    public Mono<ServerResponse> register(ServerRequest request) {
        String username = request.queryParam("username").orElseThrow();
        String password = request.queryParam("password").orElseThrow();

        Mono<ServerResponse> registerMono = Mono.defer(() ->
            databaseClient.sql("insert into user(username, password) values(:username, :password)")
                .bind("username", username)
                .bind("password", password)
                .fetch()
                .rowsUpdated()
                .flatMap(e -> (e == 1)
                    ? ServerResponse.ok().bodyValue("register succeed!")
                    : ServerResponse.badRequest().bodyValue("register failed!")));

        return databaseClient.sql("select count(1) as count from user where username=:username")
            .bind("username", username)
            .fetch().one()
            .flatMap(row ->
                (Integer.parseInt(row.get("count").toString()) > 0)
                    ? ServerResponse.badRequest().bodyValue("username exists")
                    : registerMono
            )
            .switchIfEmpty(registerMono);
    }

}
