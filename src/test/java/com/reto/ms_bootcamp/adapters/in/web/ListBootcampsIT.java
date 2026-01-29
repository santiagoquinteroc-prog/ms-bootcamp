package com.reto.ms_bootcamp.adapters.in.web;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.reto.ms_bootcamp.adapters.in.web.dto.request.CreateBootcampRequest;
import com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampListResponse;
import com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampListItemResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

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

    private WireMockServer wireMockServer;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> 
            String.format("r2dbc:mysql://%s:%d/bootcamp_db", 
                mysql.getHost(), mysql.getFirstMappedPort()));
        registry.add("spring.r2dbc.username", () -> mysql.getUsername());
        registry.add("spring.r2dbc.password", () -> mysql.getPassword());
        registry.add("ms.capacidad.url", () -> "http://localhost:8081");
    }

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8081);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8081);
        
        wireMockServer.stubFor(get(urlMatching("/capacidades/\\d+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"nombre\":\"Capacidad Default\",\"tecnologias\":[]}")));
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void shouldListBootcampsWithPagination() {
        createTestBootcamp("Bootcamp A", Arrays.asList(1L, 2L));
        createTestBootcamp("Bootcamp B", Arrays.asList(1L));

        stubCapacidad(1L, "Capacidad 1", Arrays.asList(
            createTecnologiaJson(1L, "Java"),
            createTecnologiaJson(2L, "Spring")
        ));
        stubCapacidad(2L, "Capacidad 2", Arrays.asList(
            createTecnologiaJson(2L, "Spring"),
            createTecnologiaJson(3L, "React")
        ));

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
        createTestBootcamp("Z Bootcamp", Arrays.asList(1L));
        createTestBootcamp("A Bootcamp", Arrays.asList(1L));

        stubCapacidad(1L, "Capacidad 1", Arrays.asList(createTecnologiaJson(1L, "Java")));

        webTestClient.get()
                .uri("/bootcamps?sortBy=nombre&direction=asc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BootcampListResponse.class)
                .value(response -> {
                    List<BootcampListItemResponse> items = response.getItems();
                    if (items.size() >= 2) {
                        assert items.get(0).getNombre().compareTo(items.get(1).getNombre()) <= 0;
                    }
                });
    }

    @Test
    void shouldOrderByCantidadCapacidadesDesc() {
        createTestBootcamp("Bootcamp 1", Arrays.asList(1L, 2L, 3L));
        createTestBootcamp("Bootcamp 2", Arrays.asList(1L));

        stubCapacidad(1L, "Capacidad 1", Arrays.asList(createTecnologiaJson(1L, "Java")));
        stubCapacidad(2L, "Capacidad 2", Arrays.asList(createTecnologiaJson(2L, "Spring")));
        stubCapacidad(3L, "Capacidad 3", Arrays.asList(createTecnologiaJson(3L, "React")));

        webTestClient.get()
                .uri("/bootcamps?sortBy=cantidadCapacidades&direction=desc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BootcampListResponse.class)
                .value(response -> {
                    List<BootcampListItemResponse> items = response.getItems();
                    if (items.size() >= 2) {
                        assert items.get(0).getCantidadCapacidades() >= items.get(1).getCantidadCapacidades();
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

    private void stubCapacidad(Long id, String nombre, List<String> tecnologiasJson) {
        String tecnologiasArray = String.join(",", tecnologiasJson);
        String responseBody = String.format(
            "{\"id\":%d,\"nombre\":\"%s\",\"tecnologias\":[%s]}",
            id, nombre, tecnologiasArray
        );

        wireMockServer.stubFor(get(urlEqualTo("/capacidades/" + id))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
    }

    private String createTecnologiaJson(Long id, String nombre) {
        return String.format("{\"id\":%d,\"nombre\":\"%s\"}", id, nombre);
    }
}

