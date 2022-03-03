package com.battleground.reactor;

import com.battleground.reactor.httpbin.HttpbinGetHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author wuxin
 */
@SpringBootApplication
public class ReactorBattlegroundApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactorBattlegroundApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> router() {
        return RouterFunctions.route()
            .GET("/echo", new HttpbinGetHandler())
            .build();
    }

}
