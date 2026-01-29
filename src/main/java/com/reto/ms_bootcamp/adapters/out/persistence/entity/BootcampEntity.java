package com.reto.ms_bootcamp.adapters.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("bootcamp")
public class BootcampEntity {
    @Id
    @Column("id")
    private Long id;

    @Column("nombre")
    private String nombre;

    @Column("descripcion")
    private String descripcion;

    @Column("fecha_lanzamiento")
    private LocalDate fechaLanzamiento;

    @Column("duracion_semanas")
    private Integer duracionSemanas;
}

