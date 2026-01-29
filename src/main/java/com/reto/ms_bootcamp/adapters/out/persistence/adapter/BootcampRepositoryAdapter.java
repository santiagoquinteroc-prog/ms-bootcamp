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

    @Override
    public Mono<Long> count() {
        return databaseClient.sql("SELECT COUNT(*) FROM bootcamp")
                .map((row, metadata) -> row.get(0, Long.class))
                .one();
    }

    @Override
    public Flux<Bootcamp> findAll(int page, int size, String sortBy, String direction) {
        if ("cantidadCapacidades".equals(sortBy)) {
            String sql = "SELECT b.*, COUNT(bc.capacidad_id) as cantidad_capacidades " +
                    "FROM bootcamp b " +
                    "LEFT JOIN bootcamp_capacidad bc ON b.id = bc.bootcamp_id " +
                    "GROUP BY b.id " +
                    "ORDER BY cantidad_capacidades " + (("desc".equalsIgnoreCase(direction)) ? "DESC" : "ASC") + " " +
                    "LIMIT :size OFFSET :offset";
            return databaseClient.sql(sql)
                    .bind("size", size)
                    .bind("offset", page * size)
                    .map((row, metadata) -> {
                        BootcampEntity entity = BootcampEntity.builder()
                                .id(row.get("id", Long.class))
                                .nombre(row.get("nombre", String.class))
                                .descripcion(row.get("descripcion", String.class))
                                .fechaLanzamiento(row.get("fecha_lanzamiento", java.time.LocalDate.class))
                                .duracionSemanas(row.get("duracion_semanas", Integer.class))
                                .build();
                        return toDomain(entity);
                    })
                    .all()
                    .flatMap(bootcamp -> bootcampCapacidadR2dbcRepository.findByBootcampId(bootcamp.getId())
                            .map(BootcampCapacidadEntity::getCapacidadId)
                            .collectList()
                            .map(capacidadIds -> {
                                bootcamp.setCapacidadIds(capacidadIds);
                                return bootcamp;
                            }));
        } else {
            String orderBy = "nombre";
            String sql = "SELECT * FROM bootcamp ORDER BY " + orderBy + " " + 
                    (("desc".equalsIgnoreCase(direction)) ? "DESC" : "ASC") + " LIMIT :size OFFSET :offset";
            return databaseClient.sql(sql)
                    .bind("size", size)
                    .bind("offset", page * size)
                    .map((row, metadata) -> {
                        BootcampEntity entity = BootcampEntity.builder()
                                .id(row.get("id", Long.class))
                                .nombre(row.get("nombre", String.class))
                                .descripcion(row.get("descripcion", String.class))
                                .fechaLanzamiento(row.get("fecha_lanzamiento", java.time.LocalDate.class))
                                .duracionSemanas(row.get("duracion_semanas", Integer.class))
                                .build();
                        return toDomain(entity);
                    })
                    .all()
                    .flatMap(bootcamp -> bootcampCapacidadR2dbcRepository.findByBootcampId(bootcamp.getId())
                            .map(BootcampCapacidadEntity::getCapacidadId)
                            .collectList()
                            .map(capacidadIds -> {
                                bootcamp.setCapacidadIds(capacidadIds);
                                return bootcamp;
                            }));
        }
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

    @Override
    public Mono<Void> deleteById(Long id) {
        return databaseClient.sql("DELETE FROM bootcamp_capacidad WHERE bootcamp_id = :bootcampId")
                .bind("bootcampId", id)
                .fetch()
                .rowsUpdated()
                .then(bootcampR2dbcRepository.deleteById(id))
                .then()
                .as(transactionalOperator::transactional);
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

