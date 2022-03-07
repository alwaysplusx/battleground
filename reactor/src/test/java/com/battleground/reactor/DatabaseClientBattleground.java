package com.battleground.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;

@Slf4j
@SpringBootTest
public class DatabaseClientBattleground {

    @Autowired
    DatabaseClient databaseClient;

    @Test
    void testQuery() {

        databaseClient.sql("select 1")
            .fetch().one()
            .doOnNext(e -> {
                log.info("select response: {}", e);
            })
            .block();

    }

}
