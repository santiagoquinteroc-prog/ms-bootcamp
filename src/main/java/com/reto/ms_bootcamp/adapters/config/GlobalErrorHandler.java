package com.reto.ms_bootcamp.adapters.config;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
public class GlobalErrorHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue("Error interno del servidor")
                .flatMap(response -> response.writeTo(exchange, null))
                .then();
    }
}

