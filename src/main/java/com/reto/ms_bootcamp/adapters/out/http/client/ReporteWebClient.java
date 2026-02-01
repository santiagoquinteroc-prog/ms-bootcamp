package com.reto.ms_bootcamp.adapters.out.http.client;

import com.reto.ms_bootcamp.adapters.out.http.client.dto.ReporteBootcampRequest;
import com.reto.ms_bootcamp.application.ports.ReporteServicePort;
import com.reto.ms_bootcamp.domain.Bootcamp;
import com.reto.ms_bootcamp.application.ports.CapacidadServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReporteWebClient implements ReporteServicePort {
    private static final Logger logger = LoggerFactory.getLogger(ReporteWebClient.class);
    private final WebClient webClient;
    private final CapacidadServicePort capacidadServicePort;

    public ReporteWebClient(
            @Value("${ms.reporte.url:http://localhost:8084}") String baseUrl,
            CapacidadServicePort capacidadServicePort) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.capacidadServicePort = capacidadServicePort;
    }

    @Override
    public Mono<Void> enviarBootcampCreado(Bootcamp bootcamp) {
        List<Long> capacidadIds = bootcamp.getCapacidadIds();
        if (capacidadIds == null || capacidadIds.isEmpty()) {
            return enviarReporte(bootcamp, List.of(), List.of());
        }

        return Flux.fromIterable(capacidadIds)
                .flatMap(capacidadServicePort::findById, 5)
                .collectList()
                .flatMap(capacidades -> {
                    List<ReporteBootcampRequest.CapacidadReporte> capacidadReportes = capacidades.stream()
                            .map(cap -> ReporteBootcampRequest.CapacidadReporte.builder()
                                    .id(cap.getId())
                                    .nombre(cap.getNombre())
                                    .build())
                            .toList();

                    Set<ReporteBootcampRequest.TecnologiaReporte> tecnologiasSet = capacidades.stream()
                            .flatMap(cap -> cap.getTecnologias() != null ? cap.getTecnologias().stream() : java.util.stream.Stream.empty())
                            .collect(Collectors.toMap(
                                    com.reto.ms_bootcamp.domain.Tecnologia::getId,
                                    tech -> ReporteBootcampRequest.TecnologiaReporte.builder()
                                            .id(tech.getId())
                                            .nombre(tech.getNombre())
                                            .build(),
                                    (existing, replacement) -> existing
                            ))
                            .values().stream().collect(Collectors.toSet());

                    List<ReporteBootcampRequest.TecnologiaReporte> tecnologias = tecnologiasSet.stream()
                            .sorted(Comparator.comparing(ReporteBootcampRequest.TecnologiaReporte::getId))
                            .toList();

                    return enviarReporte(bootcamp, capacidadReportes, tecnologias);
                })
                .onErrorResume(error -> {
                    logger.error("Error al enviar bootcamp creado a ms-reporte: {}", error.getMessage(), error);
                    return Mono.empty();
                });
    }

    private Mono<Void> enviarReporte(Bootcamp bootcamp,
                                     List<ReporteBootcampRequest.CapacidadReporte> capacidades,
                                     List<ReporteBootcampRequest.TecnologiaReporte> tecnologias) {
        ReporteBootcampRequest request = ReporteBootcampRequest.builder()
                .bootcampId(bootcamp.getId())
                .nombre(bootcamp.getNombre())
                .descripcion(bootcamp.getDescripcion())
                .fechaLanzamiento(bootcamp.getFechaLanzamiento())
                .duracionSemanas(bootcamp.getDuracionSemanas())
                .capacidades(capacidades)
                .tecnologias(tecnologias)
                .build();

        return webClient.post()
                .uri("/reportes/bootcamps")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(error -> {
                    logger.error("Error al enviar POST a ms-reporte para bootcamp {}: {}", bootcamp.getId(), error.getMessage(), error);
                    return Mono.empty();
                });
    }
}

