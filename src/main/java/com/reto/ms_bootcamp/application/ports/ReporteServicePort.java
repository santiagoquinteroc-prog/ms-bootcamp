package com.reto.ms_bootcamp.application.ports;

import com.reto.ms_bootcamp.domain.Bootcamp;
import reactor.core.publisher.Mono;

public interface ReporteServicePort {
    Mono<Void> enviarBootcampCreado(Bootcamp bootcamp);
}

