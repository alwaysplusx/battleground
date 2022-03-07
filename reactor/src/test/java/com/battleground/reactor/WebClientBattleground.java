package com.battleground.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * @author wuxin
 */
@Slf4j
public class WebClientBattleground {

    public static void main(String[] args) throws InterruptedException {
        WebClient.create()
            .get()
            .uri("https://httpbin.org/get?msg=echo")
            .retrieve()
            .bodyToMono(String.class)
            .doOnNext(s -> {
                log.info("get response: {}", s);
            })
            .subscribe();
        Thread.sleep(5_000);
        System.out.println(">>>>>>>>>> terminated");
    }

    @Test
    void test() {
        Mono<ResponseEntity<String>> responseEntity = WebClient.create()
            .get()
            .uri("https://httpbin.org/get")
            .retrieve()
            .toEntity(String.class)
            .doOnNext(e -> {
                log.info("response body: {}", e);
            });

        StepVerifier.create(responseEntity)
            .expectNextMatches(e -> MediaType.APPLICATION_JSON.equals(e.getHeaders().getContentType()))
            .verifyComplete();
    }

    @Test
    void testDelay() {
        Mono<String> mono = Mono.just("message@" + System.currentTimeMillis())
            .delayElement(Duration.ofSeconds(1))
            .doOnNext(e -> {
                log.info("receive message at {}. message={}", System.currentTimeMillis(), e);
            });
        StepVerifier.create(mono)
            .expectNextCount(1)
            .verifyComplete();
    }

}
