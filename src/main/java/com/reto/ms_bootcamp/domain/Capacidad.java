package com.reto.ms_bootcamp.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Capacidad {
    private Long id;
    private String nombre;
    private List<Tecnologia> tecnologias;
}

