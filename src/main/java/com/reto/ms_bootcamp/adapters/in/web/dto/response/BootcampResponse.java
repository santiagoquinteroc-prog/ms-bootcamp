package com.reto.ms_bootcamp.adapters.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BootcampResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaLanzamiento;
    private Integer duracionSemanas;
    private List<Long> capacidadIds;
}

