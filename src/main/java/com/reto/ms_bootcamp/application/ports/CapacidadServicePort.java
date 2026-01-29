package com.reto.ms_bootcamp.application.ports;

import reactor.core.publisher.Mono;

public interface CapacidadServicePort {
    Mono<Boolean> existsById(Long id);
}

