package com.reto.ms_bootcamp.adapters.in.web;

import com.reto.ms_bootcamp.adapters.in.web.dto.request.CreateBootcampRequest;
import com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampListResponse;
import com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampListItemResponse;
import com.reto.ms_bootcamp.application.ports.CapacidadServicePort;
import com.reto.ms_bootcamp.domain.Capacidad;
import com.reto.ms_bootcamp.domain.Tecnologia;
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
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class ListBootcampsIT {

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
                "r2dbc:mysql://" + mysql.getHost() + ":" + mysql.getMappedPort(3306) + "/bootcamp_db");
        registry.add("spring.r2dbc.username", mysql::getUsername);
        registry.add("spring.r2dbc.password", mysql::getPassword);
    }

    @BeforeEach
    void setUp() {
        when(capacidadServicePort.existsById(anyLong())).thenReturn(Mono.just(true));
        when(capacidadServicePort.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return Mono.just(Capacidad.builder()
                    .id(id)
                    .nombre("Capacidad " + id)
                    .tecnologias(Arrays.asList(
                            Tecnologia.builder().id(id).nombre("Tecnologia " + id).build()
                    ))
                    .build());
        });
    }

    @Test
    void shouldListBootcampsWithPagination() {
        when(capacidadServicePort.findById(1L)).thenReturn(Mono.just(Capacidad.builder()
                .id(1L)
                .nombre("Capacidad 1")
                .tecnologias(Arrays.asList(
                        Tecnologia.builder().id(1L).nombre("Java").build(),
                        Tecnologia.builder().id(2L).nombre("Spring").build()
                ))
                .build()));
        when(capacidadServicePort.findById(2L)).thenReturn(Mono.just(Capacidad.builder()
                .id(2L)
                .nombre("Capacidad 2")
                .tecnologias(Arrays.asList(
                        Tecnologia.builder().id(2L).nombre("Spring").build(),
                        Tecnologia.builder().id(3L).nombre("React").build()
                ))
                .build()));

        createTestBootcamp("Bootcamp A", Arrays.asList(1L, 2L));
        createTestBootcamp("Bootcamp B", Arrays.asList(1L));

        webTestClient.get()
                .uri("/bootcamps?page=0&size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BootcampListResponse.class)
                .value(response -> {
                    assert response.getPage().equals(0);
                    assert response.getSize().equals(10);
                    assert response.getTotalElements() >= 2;
                    assert response.getItems().size() >= 2;
                });
    }

    @Test
    void shouldOrderByNombreAsc() {
        when(capacidadServicePort.findById(1L)).thenReturn(Mono.just(Capacidad.builder()
                .id(1L)
                .nombre("Capacidad 1")
                .tecnologias(Arrays.asList(Tecnologia.builder().id(1L).nombre("Java").build()))
                .build()));

        createTestBootcamp("ZZZ Bootcamp", Arrays.asList(1L));
        createTestBootcamp("AAA Bootcamp", Arrays.asList(1L));

        webTestClient.get()
                .uri("/bootcamps?sortBy=nombre&direction=asc&size=20")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BootcampListResponse.class)
                .value(response -> {
                    List<BootcampListItemResponse> items = response.getItems();
                    boolean foundAAA = false;
                    boolean foundZZZ = false;
                    int aaaIndex = -1;
                    int zzzIndex = -1;
                    
                    for (int i = 0; i < items.size(); i++) {
                        String nombre = items.get(i).getNombre();
                        if ("AAA Bootcamp".equals(nombre)) {
                            foundAAA = true;
                            aaaIndex = i;
                        }
                        if ("ZZZ Bootcamp".equals(nombre)) {
                            foundZZZ = true;
                            zzzIndex = i;
                        }
                    }
                    
                    if (foundAAA && foundZZZ) {
                        assert aaaIndex < zzzIndex : "AAA Bootcamp debe aparecer antes que ZZZ Bootcamp en orden ascendente";
                    }
                    
                    for (int i = 0; i < items.size() - 1; i++) {
                        String nombre1 = items.get(i).getNombre();
                        String nombre2 = items.get(i + 1).getNombre();
                        assert nombre1.compareToIgnoreCase(nombre2) <= 0 
                            : String.format("Los items deben estar ordenados ascendentemente: %s debe estar antes que %s", nombre1, nombre2);
                    }
                });
    }

    @Test
    void shouldOrderByCantidadCapacidadesDesc() {
        createTestBootcamp("Bootcamp Many", Arrays.asList(1L, 2L, 3L));
        createTestBootcamp("Bootcamp Few", Arrays.asList(1L));

        webTestClient.get()
                .uri("/bootcamps?sortBy=cantidadCapacidades&direction=desc&size=20")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BootcampListResponse.class)
                .value(response -> {
                    List<BootcampListItemResponse> items = response.getItems();
                    boolean foundMany = false;
                    boolean foundFew = false;
                    int manyIndex = -1;
                    int fewIndex = -1;
                    
                    for (int i = 0; i < items.size(); i++) {
                        String nombre = items.get(i).getNombre();
                        if ("Bootcamp Many".equals(nombre)) {
                            foundMany = true;
                            manyIndex = i;
                        }
                        if ("Bootcamp Few".equals(nombre)) {
                            foundFew = true;
                            fewIndex = i;
                        }
                    }
                    
                    if (foundMany && foundFew) {
                        assert manyIndex < fewIndex : "Bootcamp Many debe aparecer antes que Bootcamp Few en orden descendente";
                    }
                    
                    for (int i = 0; i < items.size() - 1; i++) {
                        assert items.get(i).getCantidadCapacidades() >= items.get(i + 1).getCantidadCapacidades()
                            : String.format("Los items deben estar ordenados descendentemente por cantidad: %d debe ser >= %d", 
                                items.get(i).getCantidadCapacidades(), items.get(i + 1).getCantidadCapacidades());
                    }
                });
    }

    private void createTestBootcamp(String nombre, List<Long> capacidadIds) {
        CreateBootcampRequest request = new CreateBootcampRequest(
                nombre,
                "Descripción " + nombre,
                LocalDate.of(2026, 2, 10),
                8,
                capacidadIds
        );

        webTestClient.post()
                .uri("/bootcamps")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

}

