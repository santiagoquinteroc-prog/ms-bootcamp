package com.reto.ms_bootcamp.adapters.out.persistence.adapter;

import com.reto.ms_bootcamp.adapters.out.persistence.entity.BootcampEntity;
import com.reto.ms_bootcamp.adapters.out.persistence.repository.BootcampR2dbcRepository;
import com.reto.ms_bootcamp.application.ports.BootcampRepositoryPort;
import com.reto.ms_bootcamp.domain.Bootcamp;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BootcampRepositoryAdapter implements BootcampRepositoryPort {
    private final BootcampR2dbcRepository bootcampR2dbcRepository;

    public BootcampRepositoryAdapter(BootcampR2dbcRepository bootcampR2dbcRepository) {
        this.bootcampR2dbcRepository = bootcampR2dbcRepository;
    }

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        return Mono.empty();
    }

    @Override
    public Mono<Bootcamp> findById(Long id) {
        return Mono.empty();
    }

    private BootcampEntity toEntity(Bootcamp bootcamp) {
        return null;
    }

    private Bootcamp toDomain(BootcampEntity entity) {
        return null;
    }
}

