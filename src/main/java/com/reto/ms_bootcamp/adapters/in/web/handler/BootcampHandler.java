package com.reto.ms_bootcamp.adapters.in.web.handler;

import com.reto.ms_bootcamp.adapters.in.web.dto.request.CreateBootcampRequest;
import com.reto.ms_bootcamp.adapters.in.web.mapper.BootcampMapper;
import com.reto.ms_bootcamp.application.usecases.CreateBootcampUseCase;
import com.reto.ms_bootcamp.application.usecases.DeleteBootcampUseCase;
import com.reto.ms_bootcamp.domain.exceptions.BootcampDuplicateException;
import com.reto.ms_bootcamp.domain.exceptions.BootcampNotFoundException;
import com.reto.ms_bootcamp.domain.exceptions.BootcampValidationException;
import com.reto.ms_bootcamp.domain.exceptions.CapacidadNotFoundException;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class BootcampHandler {
    private final CreateBootcampUseCase createBootcampUseCase;
    private final com.reto.ms_bootcamp.application.usecases.ListBootcampsUseCase listBootcampsUseCase;
    private final DeleteBootcampUseCase deleteBootcampUseCase;
    private final Validator validator;

    public BootcampHandler(CreateBootcampUseCase createBootcampUseCase, 
                          com.reto.ms_bootcamp.application.usecases.ListBootcampsUseCase listBootcampsUseCase,
                          DeleteBootcampUseCase deleteBootcampUseCase,
                          Validator validator) {
        this.createBootcampUseCase = createBootcampUseCase;
        this.listBootcampsUseCase = listBootcampsUseCase;
        this.deleteBootcampUseCase = deleteBootcampUseCase;
        this.validator = validator;
    }

    public Mono<ServerResponse> createBootcamp(ServerRequest request) {
        return request.bodyToMono(CreateBootcampRequest.class)
                .flatMap(body -> {
                    var violations = validator.validate(body);
                    if (!violations.isEmpty()) {
                        String errorMessage = violations.stream()
                                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                .reduce((a, b) -> a + "; " + b)
                                .orElse("Error de validación");
                        return Mono.error(new BootcampValidationException(errorMessage));
                    }
                    return Mono.just(body);
                })
                .map(BootcampMapper::toDomain)
                .flatMap(createBootcampUseCase::execute)
                .map(BootcampMapper::toResponse)
                .flatMap(bootcampResponse -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(bootcampResponse))
                .onErrorResume(BootcampValidationException.class, error ->
                        ServerResponse.status(HttpStatus.BAD_REQUEST)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(error.getMessage()))
                .onErrorResume(CapacidadNotFoundException.class, error ->
                        ServerResponse.status(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(error.getMessage()))
                .onErrorResume(BootcampDuplicateException.class, error ->
                        ServerResponse.status(HttpStatus.CONFLICT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(error.getMessage()))
                .onErrorResume(error -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Error interno del servidor"));
    }

    public Mono<ServerResponse> listBootcamps(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(10);
        String sortBy = request.queryParam("sortBy").orElse("nombre");
        String direction = request.queryParam("direction").orElse("asc");

        return listBootcampsUseCase.execute(page, size, sortBy, direction)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(com.reto.ms_bootcamp.domain.exceptions.BootcampServiceException.class, error ->
                        ServerResponse.status(HttpStatus.BAD_GATEWAY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(error.getMessage()))
                .onErrorResume(error -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Error interno del servidor"));
    }

    public Mono<ServerResponse> deleteBootcamp(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return deleteBootcampUseCase.execute(id)
                .then(ServerResponse.noContent().build())
                .onErrorResume(BootcampNotFoundException.class, error ->
                        ServerResponse.status(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(error.getMessage()))
                .onErrorResume(error -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("Error interno del servidor"));
    }
}

