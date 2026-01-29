package com.reto.ms_bootcamp.adapters.in.web.handler;

import com.reto.ms_bootcamp.adapters.in.web.dto.request.CreateBootcampRequest;
import com.reto.ms_bootcamp.adapters.in.web.mapper.BootcampMapper;
import com.reto.ms_bootcamp.application.usecases.CreateBootcampUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class BootcampHandler {
    private final CreateBootcampUseCase createBootcampUseCase;

    public BootcampHandler(CreateBootcampUseCase createBootcampUseCase) {
        this.createBootcampUseCase = createBootcampUseCase;
    }

    public Mono<ServerResponse> createBootcamp(ServerRequest request) {
        return request.bodyToMono(CreateBootcampRequest.class)
                .map(BootcampMapper::toDomain)
                .flatMap(createBootcampUseCase::execute)
                .map(BootcampMapper::toResponse)
                .flatMap(bootcampResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(bootcampResponse))
                .onErrorResume(error -> ServerResponse.badRequest().build());
    }
}

