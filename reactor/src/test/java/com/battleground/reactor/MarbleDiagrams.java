package com.battleground.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class MarbleDiagrams {


    public static void main(String[] args) {

        String data = "data";

        Mono<String> mono0 = Mono.just(data);

        Mono<String> mono1 = Mono.fromSupplier(() -> data);
        Mono<String> mono2 = Mono.fromCallable(() -> {
            throw new Exception();
        });

        Mono<String> mono3 = Mono.defer(() -> Mono.just(data));

        Mono<String> mono4 = Mono.from(Mono.just(data));

    }


    @Test
    void blockResourceMono() throws Exception {
        String text = StreamUtils.copyToString(new FileInputStream("./pom.xml"), StandardCharsets.UTF_8);

        Mono<String> blockResourceMono = Mono.fromCallable(() -> {
                FileInputStream is = new FileInputStream("./pom.xml");
                return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
            })
            .doOnNext(e -> {
                log.info("block resource text: {}", e.replaceAll("\r\n", ""));
            })
            .subscribeOn(Schedulers.boundedElastic());

        StepVerifier.create(blockResourceMono)
            .expectNext(text)
            .verifyComplete();

        Mono<String> loadedResourceMono = Mono.fromCallable(() -> text)
            .doOnNext(e -> {
                log.info("loaded resource text: {}", e.replaceAll("\r\n", ""));
            });
        StepVerifier.create(loadedResourceMono)
            .expectNext(text)
            .verifyComplete();
    }

    @Test
    void chainInvoke() {
        Flux<String> flux0 = Flux.just("foo", "bar");
        flux0.map(s -> s.replaceAll("bar", "foo"));
        flux0.subscribe(next -> System.out.println("Received: " + next));

        StepVerifier.create(flux0)
            .expectNext("foo", "bar")
            .verifyComplete();

        Flux<String> flux1 = Flux.just("foo", "bar")
            .map(s -> s.replaceAll("bar", "foo"));
        flux1.subscribe(next -> System.out.println("Received: " + next));
        StepVerifier.create(flux1)
            .expectNext("foo", "foo")
            .verifyComplete();
    }

}
