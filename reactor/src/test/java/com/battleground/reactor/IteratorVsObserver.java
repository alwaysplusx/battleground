package com.battleground.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Slf4j
class IteratorVsObserver {

    @Test
    void testIterator() {
        List<Integer> array = Arrays.asList(1, 2, 3, 4, 5, 6);
        for (Integer val : array) {
            log.info("receive num: {}", val);
        }
    }

    @Test
    void testObserver() throws Exception {
        Flux<Integer> flux = Flux.range(1, 6)
            .doOnNext(val -> {
                log.info("receive num: {}", val);
            });
        Thread.sleep(10_000);
        // flux.blockLast();
    }

}
