package com.reto.ms_bootcamp.adapters.in.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBootcampRequest {
    @NotBlank(message = "El nombre es requerido")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;

    @Size(max = 90, message = "La descripción no puede exceder 90 caracteres")
    private String descripcion;

    @NotNull(message = "La fecha de lanzamiento es requerida")
    private LocalDate fechaLanzamiento;

    @NotNull(message = "La duración en semanas es requerida")
    private Integer duracionSemanas;

    @NotEmpty(message = "Debe tener al menos una capacidad")
    private List<Long> capacidadIds;
}

