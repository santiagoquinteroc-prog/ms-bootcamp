package com.reto.ms_bootcamp.adapters.in.web.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBootcampRequest {
    private String name;
    private String description;
}

