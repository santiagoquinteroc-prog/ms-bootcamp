package com.reto.ms_bootcamp.adapters.in.web.router;

import com.reto.ms_bootcamp.adapters.in.web.handler.BootcampHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class BootcampRouter {
    @Bean
    public RouterFunction<ServerResponse> bootcampRoutes(BootcampHandler bootcampHandler) {
        return RouterFunctions.route()
                .POST("/api/bootcamps", accept(org.springframework.http.MediaType.APPLICATION_JSON), bootcampHandler::createBootcamp)
                .build();
    }
}

