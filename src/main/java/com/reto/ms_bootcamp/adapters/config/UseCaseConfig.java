package com.reto.ms_bootcamp.adapters.config;

import com.reto.ms_bootcamp.application.ports.BootcampRepositoryPort;
import com.reto.ms_bootcamp.application.usecases.CreateBootcampUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean
    public CreateBootcampUseCase createBootcampUseCase(BootcampRepositoryPort bootcampRepositoryPort) {
        return new CreateBootcampUseCase(bootcampRepositoryPort);
    }
}

