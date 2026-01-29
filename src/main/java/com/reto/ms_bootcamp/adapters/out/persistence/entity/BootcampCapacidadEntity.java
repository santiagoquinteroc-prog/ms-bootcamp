package com.reto.ms_bootcamp.adapters.out.persistence.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("bootcamp_capacidad")
public class BootcampCapacidadEntity {
    @Column("bootcamp_id")
    private Long bootcampId;

    @Column("capacidad_id")
    private Long capacidadId;
}

