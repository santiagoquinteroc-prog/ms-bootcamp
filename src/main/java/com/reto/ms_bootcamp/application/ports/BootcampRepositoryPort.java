package com.reto.ms_bootcamp.application.ports;

import com.reto.ms_bootcamp.domain.Bootcamp;
import reactor.core.publisher.Mono;

public interface BootcampRepositoryPort {
    Mono<Bootcamp> save(Bootcamp bootcamp);
    Mono<Bootcamp> findById(Long id);
}

