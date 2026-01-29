package com.reto.ms_bootcamp.domain.rules;

import com.reto.ms_bootcamp.domain.exceptions.BootcampValidationException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BootcampRules {
    private static final int MIN_CAPACIDADES = 1;
    private static final int MAX_CAPACIDADES = 4;

    public static void validateCapacidades(List<Long> capacidadIds) {
        if (capacidadIds == null || capacidadIds.isEmpty()) {
            throw new BootcampValidationException("Debe tener al menos una capacidad");
        }

        if (capacidadIds.size() < MIN_CAPACIDADES || capacidadIds.size() > MAX_CAPACIDADES) {
            throw new BootcampValidationException("Debe tener entre " + MIN_CAPACIDADES + " y " + MAX_CAPACIDADES + " capacidades");
        }

        Set<Long> uniqueIds = new HashSet<>(capacidadIds);
        if (uniqueIds.size() != capacidadIds.size()) {
            throw new BootcampValidationException("Las capacidades no pueden estar repetidas");
        }
    }
}

