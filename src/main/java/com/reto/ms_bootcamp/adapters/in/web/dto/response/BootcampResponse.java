package com.reto.ms_bootcamp.adapters.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BootcampResponse {
    private Long id;
    private String name;
    private String description;
}

