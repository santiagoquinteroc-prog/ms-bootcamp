package com.reto.ms_bootcamp.adapters.out.persistence.repository;

import com.reto.ms_bootcamp.adapters.out.persistence.entity.BootcampCapacidadEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface BootcampCapacidadR2dbcRepository extends R2dbcRepository<BootcampCapacidadEntity, Long> {
    Flux<BootcampCapacidadEntity> findByBootcampId(Long bootcampId);
}

