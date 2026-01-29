package com.reto.ms_bootcamp.application.usecases;

import com.reto.ms_bootcamp.application.ports.BootcampRepositoryPort;
import com.reto.ms_bootcamp.application.ports.CapacidadServicePort;
import com.reto.ms_bootcamp.domain.Bootcamp;
import com.reto.ms_bootcamp.domain.exceptions.BootcampDuplicateException;
import com.reto.ms_bootcamp.domain.exceptions.BootcampValidationException;
import com.reto.ms_bootcamp.domain.exceptions.CapacidadNotFoundException;
import com.reto.ms_bootcamp.domain.rules.BootcampRules;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class CreateBootcampUseCase {
    private final BootcampRepositoryPort bootcampRepositoryPort;
    private final CapacidadServicePort capacidadServicePort;

    public CreateBootcampUseCase(BootcampRepositoryPort bootcampRepositoryPort, CapacidadServicePort capacidadServicePort) {
        this.bootcampRepositoryPort = bootcampRepositoryPort;
        this.capacidadServicePort = capacidadServicePort;
    }

    public Mono<Bootcamp> execute(Bootcamp bootcamp) {
        return validateNombreUnico(bootcamp.getNombre())
                .then(validateCapacidades(bootcamp.getCapacidadIds()))
                .then(validateCapacidadesExisten(bootcamp.getCapacidadIds()))
                .then(bootcampRepositoryPort.save(bootcamp));
    }

    private Mono<Void> validateNombreUnico(String nombre) {
        return bootcampRepositoryPort.existsByNombre(nombre)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BootcampDuplicateException("El nombre del bootcamp ya existe"));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> validateCapacidades(List<Long> capacidadIds) {
        try {
            BootcampRules.validateCapacidades(capacidadIds);
            return Mono.empty();
        } catch (BootcampValidationException e) {
            return Mono.error(e);
        }
    }

    private Mono<Void> validateCapacidadesExisten(List<Long> capacidadIds) {
        return Flux.fromIterable(capacidadIds)
                .flatMap(capacidadId -> capacidadServicePort.existsById(capacidadId)
                        .flatMap(exists -> {
                            if (!exists) {
                                return Mono.error(new CapacidadNotFoundException("La capacidad con id " + capacidadId + " no existe"));
                            }
                            return Mono.just(true);
                        })
                        .onErrorMap(CapacidadNotFoundException.class, error -> error))
                .collectList()
                .then();
    }
}

