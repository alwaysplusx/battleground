package com.battleground.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.reactive.TransactionalOperator;

@Slf4j
@SpringBootTest(classes = ReactorBattlegroundApplication.class)
public class DatabaseClientBattleground {

    @Autowired
    DatabaseClient databaseClient;

    @Autowired
    TransactionalOperator transactionalOperator;

    @Test
    void testQuery() {
        databaseClient.sql("select 1")
            .fetch().one()
            .doOnNext(e -> {
                log.info("select response: {}", e);
            })
            .block();
    }

    @Test
    void testTransaction() {
        transactionalOperator
            .transactional(
                databaseClient.sql("insert into user(username, password) values(:username, :password)")
                    .bind("username", "error_username")
                    .bind("password", "error_password")
                    .fetch().rowsUpdated()
                    .doOnNext(e -> {
                        // throw new RuntimeException("custom exception");
                    })
            )
            .block();
    }

    @Test
    void testWithoutTransaction() {
        databaseClient.sql("insert into user(username, password) values(:username, :password)")
            .bind("username", "without_tx_username")
            .bind("password", "without_tx_password")
            .fetch().rowsUpdated()
            .doOnNext(e -> {
                throw new RuntimeException("custom exception");
            })
            .block();
    }

}
