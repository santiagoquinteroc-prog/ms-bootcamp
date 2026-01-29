package com.reto.ms_bootcamp.application.ports;

import com.reto.ms_bootcamp.domain.Capacidad;
import reactor.core.publisher.Mono;

public interface CapacidadServicePort {
    Mono<Boolean> existsById(Long id);
    Mono<Capacidad> findById(Long id);
}
