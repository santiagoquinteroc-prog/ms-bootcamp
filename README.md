# MS Bootcamp

Microservicio de gestión de bootcamp con arquitectura hexagonal usando Spring Boot WebFlux y R2DBC.

## Requisitos

- Java 17
- Gradle
- Docker y Docker Compose

## Configuración de Base de Datos

Levantar MySQL con Docker Compose:

```bash
docker-compose up -d
```

La base de datos se inicializará automáticamente con las tablas necesarias.

## Ejecutar la Aplicación

```bash
./gradlew bootRun
```

La aplicación estará disponible en `http://localhost:8082`

API Documentation: `http://localhost:8082/swagger-ui.html`

