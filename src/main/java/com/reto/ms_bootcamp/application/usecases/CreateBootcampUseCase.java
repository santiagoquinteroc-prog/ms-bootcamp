package com.reto.ms_bootcamp.application.usecases;

import com.reto.ms_bootcamp.application.ports.BootcampRepositoryPort;
import com.reto.ms_bootcamp.domain.Bootcamp;
import reactor.core.publisher.Mono;

public class CreateBootcampUseCase {
    private final BootcampRepositoryPort bootcampRepositoryPort;

    public CreateBootcampUseCase(BootcampRepositoryPort bootcampRepositoryPort) {
        this.bootcampRepositoryPort = bootcampRepositoryPort;
    }

    public Mono<Bootcamp> execute(Bootcamp bootcamp) {
        return Mono.empty();
    }
}

