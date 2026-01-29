package com.reto.ms_bootcamp.adapters.in.web.mapper;

import com.reto.ms_bootcamp.adapters.in.web.dto.request.CreateBootcampRequest;
import com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampResponse;
import com.reto.ms_bootcamp.domain.Bootcamp;

public class BootcampMapper {
    public static Bootcamp toDomain(CreateBootcampRequest request) {
        return Bootcamp.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .fechaLanzamiento(request.getFechaLanzamiento())
                .duracionSemanas(request.getDuracionSemanas())
                .capacidadIds(request.getCapacidadIds())
                .build();
    }

    public static BootcampResponse toResponse(Bootcamp bootcamp) {
        return BootcampResponse.builder()
                .id(bootcamp.getId())
                .nombre(bootcamp.getNombre())
                .descripcion(bootcamp.getDescripcion())
                .fechaLanzamiento(bootcamp.getFechaLanzamiento())
                .duracionSemanas(bootcamp.getDuracionSemanas())
                .capacidadIds(bootcamp.getCapacidadIds())
                .build();
    }
}

