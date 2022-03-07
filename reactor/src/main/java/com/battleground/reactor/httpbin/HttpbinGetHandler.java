package com.battleground.reactor.httpbin;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
public class HttpbinGetHandler implements HandlerFunction<ServerResponse> {

    WebClient webClient = WebClient.builder().baseUrl("https://httpbin.org").build();

    @NotNull
    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        log.info("handle httpbin get request: {}", request.queryParams());
        return webClient.get()
            .uri(uriBuilder -> {
                request.queryParams().forEach(uriBuilder::queryParam);
                return uriBuilder.path("/get").build();
            })
            .retrieve()
            .toEntity(String.class)
            .doOnNext(responseEntity -> {
                log.info("httpbin response: {}", responseEntity.getBody());
            })
            .flatMap(responseEntity ->
                ServerResponse.ok()
                    .headers(headers -> headers.putAll(responseEntity.getHeaders()))
                    .body(BodyInserters.fromValue(responseEntity.getBody())));
    }

}
