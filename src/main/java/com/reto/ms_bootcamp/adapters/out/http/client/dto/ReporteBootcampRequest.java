package com.reto.ms_bootcamp.adapters.out.http.client.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ReporteBootcampRequest {
    private Long bootcampId;
    private String nombre;
    private String descripcion;
    private LocalDate fechaLanzamiento;
    private Integer duracionSemanas;
    private List<CapacidadReporte> capacidades;
    private List<TecnologiaReporte> tecnologias;

    @Data
    @Builder
    public static class CapacidadReporte {
        private Long id;
        private String nombre;
    }

    @Data
    @Builder
    public static class TecnologiaReporte {
        private Long id;
        private String nombre;
    }
}

