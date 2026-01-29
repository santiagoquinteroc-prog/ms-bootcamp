package com.reto.ms_bootcamp.adapters.config;

import com.reto.ms_bootcamp.application.ports.BootcampRepositoryPort;
import com.reto.ms_bootcamp.application.ports.CapacidadServicePort;
import com.reto.ms_bootcamp.application.usecases.CreateBootcampUseCase;
import com.reto.ms_bootcamp.application.usecases.DeleteBootcampUseCase;
import com.reto.ms_bootcamp.application.usecases.ListBootcampsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean
    public CreateBootcampUseCase createBootcampUseCase(
            BootcampRepositoryPort bootcampRepositoryPort,
            CapacidadServicePort capacidadServicePort) {
        return new CreateBootcampUseCase(bootcampRepositoryPort, capacidadServicePort);
    }

    @Bean
    public ListBootcampsUseCase listBootcampsUseCase(
            BootcampRepositoryPort bootcampRepositoryPort,
            CapacidadServicePort capacidadServicePort) {
        return new ListBootcampsUseCase(bootcampRepositoryPort, capacidadServicePort);
    }

    @Bean
    public DeleteBootcampUseCase deleteBootcampUseCase(
            BootcampRepositoryPort bootcampRepositoryPort) {
        return new DeleteBootcampUseCase(bootcampRepositoryPort);
    }
}

