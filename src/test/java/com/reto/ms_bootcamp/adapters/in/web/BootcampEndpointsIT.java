package com.reto.ms_bootcamp.adapters.in.web;

import com.reto.ms_bootcamp.adapters.in.web.dto.request.CreateBootcampRequest;
import com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampResponse;
import com.reto.ms_bootcamp.application.ports.CapacidadServicePort;
import com.reto.ms_bootcamp.domain.exceptions.CapacidadNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class BootcampEndpointsIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("bootcamp_db")
            .withUsername("app")
            .withPassword("app")
            .withInitScript("init.sql");

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CapacidadServicePort capacidadServicePort;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> 
            String.format("r2dbc:mysql://%s:%d/bootcamp_db", 
                mysql.getHost(), mysql.getFirstMappedPort()));
        registry.add("spring.r2dbc.username", () -> mysql.getUsername());
        registry.add("spring.r2dbc.password", () -> mysql.getPassword());
    }

    @BeforeEach
    void setUp() {
        when(capacidadServicePort.existsById(anyLong())).thenReturn(Mono.just(true));
    }

    @Test
    void shouldCreateBootcampWithCapacidadIds() {
        CreateBootcampRequest request = new CreateBootcampRequest(
                "Bootcamp Test",
                "Descripción test",
                LocalDate.of(2026, 2, 10),
                8,
                Arrays.asList(1L, 2L)
        );

        webTestClient.post()
                .uri("/bootcamps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(BootcampResponse.class)
                .value(response -> {
                    assert response.getId() != null;
                    assert response.getNombre().equals("Bootcamp Test");
                    assert response.getDescripcion().equals("Descripción test");
                    assert response.getFechaLanzamiento().equals(LocalDate.of(2026, 2, 10));
                    assert response.getDuracionSemanas().equals(8);
                    assert response.getCapacidadIds() != null;
                    assert response.getCapacidadIds().size() == 2;
                    assert response.getCapacidadIds().containsAll(Arrays.asList(1L, 2L));
                });
    }

    @Test
    void shouldReturnExactCapacidadIdsAsSent() {
        List<Long> expectedCapacidadIds = Arrays.asList(1L, 2L);
        
        CreateBootcampRequest request = new CreateBootcampRequest(
                "Bootcamp Exact Test",
                "Test exactitud",
                LocalDate.of(2026, 3, 15),
                12,
                expectedCapacidadIds
        );

        webTestClient.post()
                .uri("/bootcamps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BootcampResponse.class)
                .value(response -> {
                    List<Long> actualCapacidadIds = response.getCapacidadIds();
                    assert actualCapacidadIds != null : "capacidadIds no debe ser null";
                    assert actualCapacidadIds.size() == expectedCapacidadIds.size() : 
                        "Debe tener " + expectedCapacidadIds.size() + " capacidades, pero tiene " + actualCapacidadIds.size();
                    assert actualCapacidadIds.containsAll(expectedCapacidadIds) : 
                        "Debe contener las capacidades " + expectedCapacidadIds + ", pero tiene " + actualCapacidadIds;
                });
    }

    @Test
    void shouldReturn404WhenCapacidadDoesNotExist() {
        when(capacidadServicePort.existsById(eq(1L))).thenReturn(Mono.just(true));
        when(capacidadServicePort.existsById(eq(999L))).thenReturn(
            Mono.error(new CapacidadNotFoundException("La capacidad con id 999 no existe"))
        );

        CreateBootcampRequest request = new CreateBootcampRequest(
                "Bootcamp Invalid",
                "Test capacidad inexistente",
                LocalDate.of(2026, 2, 10),
                8,
                Arrays.asList(1L, 999L)
        );

        webTestClient.post()
                .uri("/bootcamps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(errorMessage -> {
                    assert errorMessage.contains("999") || errorMessage.contains("no existe");
                });
    }

    @Test
    void shouldNotCreateBootcampWhenCapacidadDoesNotExist() {
        when(capacidadServicePort.existsById(eq(1L))).thenReturn(Mono.just(true));
        when(capacidadServicePort.existsById(eq(999L))).thenReturn(
            Mono.error(new CapacidadNotFoundException("La capacidad con id 999 no existe"))
        );

        CreateBootcampRequest request = new CreateBootcampRequest(
                "Bootcamp No Debe Crearse",
                "Este bootcamp no debe persistirse",
                LocalDate.of(2026, 2, 10),
                8,
                Arrays.asList(1L, 999L)
        );

        webTestClient.post()
                .uri("/bootcamps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class)
                .value(errorMessage -> {
                    assert errorMessage.contains("999") || errorMessage.contains("no existe");
                });

        CreateBootcampRequest validRequest = new CreateBootcampRequest(
                "Bootcamp No Debe Crearse",
                "Verificar que no existe",
                LocalDate.of(2026, 2, 10),
                8,
                Arrays.asList(1L, 2L)
        );

        when(capacidadServicePort.existsById(eq(2L))).thenReturn(Mono.just(true));

        webTestClient.post()
                .uri("/bootcamps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void shouldValidateAllCapacidadesBeforePersisting() {
        when(capacidadServicePort.existsById(eq(1L))).thenReturn(Mono.just(true));
        when(capacidadServicePort.existsById(eq(2L))).thenReturn(Mono.just(true));
        when(capacidadServicePort.existsById(eq(999L))).thenReturn(
            Mono.error(new CapacidadNotFoundException("La capacidad con id 999 no existe"))
        );

        CreateBootcampRequest request = new CreateBootcampRequest(
                "Bootcamp Multiple Invalid",
                "Test validación múltiple",
                LocalDate.of(2026, 2, 10),
                8,
                Arrays.asList(1L, 2L, 999L)
        );

        webTestClient.post()
                .uri("/bootcamps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound();
    }
}

