package com.reto.ms_bootcamp.adapters.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BootcampListResponse {
    private Integer page;
    private Integer size;
    private Long totalElements;
    private List<BootcampListItemResponse> items;
}

