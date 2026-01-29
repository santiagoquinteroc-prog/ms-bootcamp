package com.reto.ms_bootcamp.adapters.out.persistence.repository;

import com.reto.ms_bootcamp.adapters.out.persistence.entity.BootcampEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface BootcampR2dbcRepository extends R2dbcRepository<BootcampEntity, Long> {
    Mono<BootcampEntity> findById(Long id);
    Mono<BootcampEntity> findByNombre(String nombre);
}

