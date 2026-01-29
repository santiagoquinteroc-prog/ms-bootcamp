package com.reto.ms_bootcamp.adapters.out.http.client;

import com.reto.ms_bootcamp.application.ports.CapacidadServicePort;
import com.reto.ms_bootcamp.domain.Capacidad;
import com.reto.ms_bootcamp.domain.Tecnologia;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

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

    @Override
    public Mono<Capacidad> findById(Long id) {
        return webClient.get()
                .uri("/capacidades/{id}", id)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Long capacidadId = Long.valueOf(response.get("id").toString());
                    String nombre = response.get("nombre").toString();
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> tecnologiasData = (List<Map<String, Object>>) response.get("tecnologias");
                    List<Tecnologia> tecnologias = tecnologiasData != null ? tecnologiasData.stream()
                            .map(tech -> Tecnologia.builder()
                                    .id(Long.valueOf(tech.get("id").toString()))
                                    .nombre(tech.get("nombre").toString())
                                    .build())
                            .toList() : List.of();
                    return Capacidad.builder()
                            .id(capacidadId)
                            .nombre(nombre)
                            .tecnologias(tecnologias)
                            .build();
                })
                .onErrorResume(org.springframework.web.reactive.function.client.WebClientResponseException.class, error -> 
                    Mono.error(new RuntimeException("Error al obtener capacidad desde ms-capacidad: " + error.getMessage())));
    }
}

