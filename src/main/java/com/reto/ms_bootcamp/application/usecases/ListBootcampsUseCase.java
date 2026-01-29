package com.reto.ms_bootcamp.application.usecases;

import com.reto.ms_bootcamp.application.ports.BootcampRepositoryPort;
import com.reto.ms_bootcamp.application.ports.CapacidadServicePort;
import com.reto.ms_bootcamp.domain.Bootcamp;
import com.reto.ms_bootcamp.domain.Capacidad;
import com.reto.ms_bootcamp.domain.Tecnologia;
import com.reto.ms_bootcamp.domain.exceptions.BootcampServiceException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListBootcampsUseCase {
    private final BootcampRepositoryPort bootcampRepositoryPort;
    private final CapacidadServicePort capacidadServicePort;

    public ListBootcampsUseCase(BootcampRepositoryPort bootcampRepositoryPort, CapacidadServicePort capacidadServicePort) {
        this.bootcampRepositoryPort = bootcampRepositoryPort;
        this.capacidadServicePort = capacidadServicePort;
    }

    public Mono<com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampListResponse> execute(int page, int size, String sortBy, String direction) {
        String finalSortBy = sortBy != null ? sortBy : "nombre";
        String finalDirection = direction != null ? direction : "asc";

        Mono<Long> totalCount = bootcampRepositoryPort.count();
        Flux<Bootcamp> bootcamps = bootcampRepositoryPort.findAll(page, size, finalSortBy, finalDirection);

        return bootcamps
                .flatMap(bootcamp -> enrichBootcampWithCapacidades(bootcamp), 5)
                .collectList()
                .zipWith(totalCount)
                .map(tuple -> {
                    List<com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampListItemResponse> items = tuple.getT1();
                    Long total = tuple.getT2();
                    return com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampListResponse.builder()
                            .page(page)
                            .size(size)
                            .totalElements(total)
                            .items(items)
                            .build();
                })
                .onErrorMap(error -> {
                    if (error instanceof BootcampServiceException) {
                        return error;
                    }
                    return new BootcampServiceException("Error al listar bootcamps: " + error.getMessage());
                });
    }

    private Mono<com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampListItemResponse> enrichBootcampWithCapacidades(Bootcamp bootcamp) {
        List<Long> capacidadIds = bootcamp.getCapacidadIds();
        if (capacidadIds == null || capacidadIds.isEmpty()) {
            return Mono.just(buildResponse(bootcamp, List.of(), List.of()));
        }

        return Flux.fromIterable(capacidadIds)
                .flatMap(capacidadServicePort::findById, 5)
                .collectList()
                .map(capacidades -> {
                    List<com.reto.ms_bootcamp.adapters.in.web.dto.response.CapacidadResponse> capacidadResponses = capacidades.stream()
                            .map(cap -> com.reto.ms_bootcamp.adapters.in.web.dto.response.CapacidadResponse.builder()
                                    .id(cap.getId())
                                    .nombre(cap.getNombre())
                                    .build())
                            .toList();

                    List<com.reto.ms_bootcamp.adapters.in.web.dto.response.TecnologiaResponse> tecnologiasList = capacidades.stream()
                            .flatMap(cap -> cap.getTecnologias() != null ? cap.getTecnologias().stream() : java.util.stream.Stream.empty())
                            .collect(Collectors.toMap(
                                    Tecnologia::getId,
                                    tech -> com.reto.ms_bootcamp.adapters.in.web.dto.response.TecnologiaResponse.builder()
                                            .id(tech.getId())
                                            .nombre(tech.getNombre())
                                            .build(),
                                    (existing, replacement) -> existing
                            ))
                            .values()
                            .stream()
                            .sorted(Comparator.comparing(com.reto.ms_bootcamp.adapters.in.web.dto.response.TecnologiaResponse::getId))
                            .toList();

                    List<com.reto.ms_bootcamp.adapters.in.web.dto.response.TecnologiaResponse> tecnologias = tecnologiasList;

                    return buildResponse(bootcamp, capacidadResponses, tecnologias);
                })
                .onErrorMap(error -> new BootcampServiceException("Error al obtener capacidades desde ms-capacidad: " + error.getMessage()));
    }

    private com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampListItemResponse buildResponse(
            Bootcamp bootcamp,
            List<com.reto.ms_bootcamp.adapters.in.web.dto.response.CapacidadResponse> capacidades,
            List<com.reto.ms_bootcamp.adapters.in.web.dto.response.TecnologiaResponse> tecnologias) {
        return com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampListItemResponse.builder()
                .id(bootcamp.getId())
                .nombre(bootcamp.getNombre())
                .descripcion(bootcamp.getDescripcion())
                .fechaLanzamiento(bootcamp.getFechaLanzamiento())
                .duracionSemanas(bootcamp.getDuracionSemanas())
                .cantidadCapacidades(capacidades.size())
                .capacidades(capacidades)
                .tecnologias(tecnologias)
                .build();
    }
}

