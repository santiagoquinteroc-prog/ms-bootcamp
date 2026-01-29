package com.reto.ms_bootcamp.adapters.out.persistence.adapter;

import com.reto.ms_bootcamp.adapters.out.persistence.entity.BootcampCapacidadEntity;
import com.reto.ms_bootcamp.adapters.out.persistence.entity.BootcampEntity;
import com.reto.ms_bootcamp.adapters.out.persistence.repository.BootcampCapacidadR2dbcRepository;
import com.reto.ms_bootcamp.adapters.out.persistence.repository.BootcampR2dbcRepository;
import com.reto.ms_bootcamp.application.ports.BootcampRepositoryPort;
import com.reto.ms_bootcamp.domain.Bootcamp;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class BootcampRepositoryAdapter implements BootcampRepositoryPort {
    private final BootcampR2dbcRepository bootcampR2dbcRepository;
    private final BootcampCapacidadR2dbcRepository bootcampCapacidadR2dbcRepository;
    private final DatabaseClient databaseClient;
    private final TransactionalOperator transactionalOperator;

    public BootcampRepositoryAdapter(
            BootcampR2dbcRepository bootcampR2dbcRepository,
            BootcampCapacidadR2dbcRepository bootcampCapacidadR2dbcRepository,
            DatabaseClient databaseClient,
            TransactionalOperator transactionalOperator) {
        this.bootcampR2dbcRepository = bootcampR2dbcRepository;
        this.bootcampCapacidadR2dbcRepository = bootcampCapacidadR2dbcRepository;
        this.databaseClient = databaseClient;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        BootcampEntity entity = toEntity(bootcamp);
        List<Long> capacidadIds = bootcamp.getCapacidadIds();
        return bootcampR2dbcRepository.save(entity)
                .flatMap(savedEntity -> {
                    Long bootcampId = savedEntity.getId();
                    return saveCapacidades(bootcampId, capacidadIds)
                            .then(Mono.just(savedEntity));
                })
                .flatMap(savedEntity -> {
                    Long bootcampId = savedEntity.getId();
                    return bootcampCapacidadR2dbcRepository.findByBootcampId(bootcampId)
                            .map(BootcampCapacidadEntity::getCapacidadId)
                            .collectList()
                            .map(savedCapacidadIds -> {
                                Bootcamp savedBootcamp = toDomain(savedEntity);
                                savedBootcamp.setCapacidadIds(savedCapacidadIds);
                                return savedBootcamp;
                            });
                })
                .as(transactionalOperator::transactional);
    }

    private Mono<Void> saveCapacidades(Long bootcampId, java.util.List<Long> capacidadIds) {
        if (capacidadIds == null || capacidadIds.isEmpty()) {
            return Mono.empty();
        }
        return Flux.fromIterable(capacidadIds)
                .map(capacidadId -> BootcampCapacidadEntity.builder()
                        .bootcampId(bootcampId)
                        .capacidadId(capacidadId)
                        .build())
                .flatMap(bootcampCapacidadR2dbcRepository::save)
                .then();
    }

    @Override
    public Mono<Bootcamp> findById(Long id) {
        return bootcampR2dbcRepository.findById(id)
                .flatMap(entity -> bootcampCapacidadR2dbcRepository.findByBootcampId(entity.getId())
                        .map(BootcampCapacidadEntity::getCapacidadId)
                        .collectList()
                        .map(capacidadIds -> {
                            Bootcamp bootcamp = toDomain(entity);
                            bootcamp.setCapacidadIds(capacidadIds);
                            return bootcamp;
                        }));
    }

    @Override
    public Mono<Boolean> existsByNombre(String nombre) {
        return bootcampR2dbcRepository.findByNombre(nombre)
                .hasElement();
    }

    private BootcampEntity toEntity(Bootcamp bootcamp) {
        return BootcampEntity.builder()
                .id(bootcamp.getId())
                .nombre(bootcamp.getNombre())
                .descripcion(bootcamp.getDescripcion())
                .fechaLanzamiento(bootcamp.getFechaLanzamiento())
                .duracionSemanas(bootcamp.getDuracionSemanas())
                .build();
    }

    private Bootcamp toDomain(BootcampEntity entity) {
        return Bootcamp.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .fechaLanzamiento(entity.getFechaLanzamiento())
                .duracionSemanas(entity.getDuracionSemanas())
                .build();
    }
}

