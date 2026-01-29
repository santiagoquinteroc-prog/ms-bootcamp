package com.reto.ms_bootcamp.adapters.out.http.client;

import com.reto.ms_bootcamp.application.ports.CapacidadServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class CapacidadWebClient implements CapacidadServicePort {
    private final WebClient webClient;

    public CapacidadWebClient(@Value("${ms.capacidad.url:http://localhost:8081}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        return webClient.get()
                .uri("/capacidades/{id}", id)
                .retrieve()
                .bodyToMono(Object.class)
                .map(response -> true)
                .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.NotFound.class, error -> 
                    Mono.error(new com.reto.ms_bootcamp.domain.exceptions.CapacidadNotFoundException("La capacidad con id " + id + " no existe")))
                .onErrorResume(error -> {
                    if (error instanceof com.reto.ms_bootcamp.domain.exceptions.CapacidadNotFoundException) {
                        return Mono.error(error);
                    }
                    return Mono.error(new RuntimeException("Error al validar capacidad: " + error.getMessage()));
                });
    }
}

