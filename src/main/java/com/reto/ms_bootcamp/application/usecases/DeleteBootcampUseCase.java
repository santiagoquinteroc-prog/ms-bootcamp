package com.reto.ms_bootcamp.application.usecases;

import com.reto.ms_bootcamp.application.ports.BootcampRepositoryPort;
import com.reto.ms_bootcamp.domain.exceptions.BootcampNotFoundException;
import reactor.core.publisher.Mono;

public class DeleteBootcampUseCase {
    private final BootcampRepositoryPort bootcampRepositoryPort;

    public DeleteBootcampUseCase(BootcampRepositoryPort bootcampRepositoryPort) {
        this.bootcampRepositoryPort = bootcampRepositoryPort;
    }

    public Mono<Void> execute(Long id) {
        return bootcampRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new BootcampNotFoundException("Bootcamp con id " + id + " no encontrado")))
                .flatMap(bootcamp -> bootcampRepositoryPort.deleteById(id));
    }
}

