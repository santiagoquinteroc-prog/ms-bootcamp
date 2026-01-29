package com.reto.ms_bootcamp.adapters.in.web.router;

import com.reto.ms_bootcamp.adapters.in.web.dto.request.CreateBootcampRequest;
import com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampResponse;
import com.reto.ms_bootcamp.adapters.in.web.handler.BootcampHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class BootcampRouter {
	@Bean
	@RouterOperations({
		@RouterOperation(
			path = "/bootcamps",
			produces = {MediaType.APPLICATION_JSON_VALUE},
			method = RequestMethod.POST,
			beanClass = BootcampHandler.class,
			beanMethod = "createBootcamp",
			operation = @Operation(
				operationId = "createBootcamp",
				summary = "Registrar bootcamp",
				tags = {"Bootcamps"},
				requestBody = @RequestBody(
					required = true,
					content = @Content(schema = @Schema(implementation = CreateBootcampRequest.class))
				),
				responses = {
					@ApiResponse(responseCode = "201", description = "Creado",
						content = @Content(schema = @Schema(implementation = BootcampResponse.class))),
					@ApiResponse(responseCode = "400", description = "Solicitud inválida"),
					@ApiResponse(responseCode = "404", description = "Capacidad no encontrada"),
					@ApiResponse(responseCode = "409", description = "Nombre duplicado")
				}
			)
		),
		@RouterOperation(
			path = "/bootcamps",
			produces = {MediaType.APPLICATION_JSON_VALUE},
			method = RequestMethod.GET,
			beanClass = BootcampHandler.class,
			beanMethod = "listBootcamps",
			operation = @Operation(
				operationId = "listBootcamps",
				summary = "Listar bootcamps",
				tags = {"Bootcamps"},
				responses = {
					@ApiResponse(responseCode = "200", description = "OK",
						content = @Content(schema = @Schema(implementation = com.reto.ms_bootcamp.adapters.in.web.dto.response.BootcampListResponse.class))),
					@ApiResponse(responseCode = "502", description = "Error en servicio de capacidades")
				}
			)
		)
	})
	public RouterFunction<ServerResponse> bootcampRoutes(BootcampHandler bootcampHandler) {
		return RouterFunctions.route(POST("/bootcamps").and(accept(MediaType.APPLICATION_JSON)), bootcampHandler::createBootcamp)
			.andRoute(GET("/bootcamps"), bootcampHandler::listBootcamps);
	}
}

