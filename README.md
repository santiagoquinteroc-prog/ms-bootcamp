# MS Bootcamp

Microservicio de gestión de bootcamp con arquitectura hexagonal usando Spring Boot WebFlux y R2DBC.

## Requisitos

- Java 17
- Gradle
- Docker y Docker Compose

## Dependencias

Este microservicio depende de:
- **ms-capacidad**: Microservicio de gestión de capacidades
  - Endpoint requerido: `GET /capacidades/{id}` para validar existencia de capacidades
  - URL configurable mediante variable de entorno `MS_CAPACIDAD_URL` (default: `http://localhost:8081`)
  - Si el endpoint no está disponible, se asume que la capacidad existe
- **ms-reporte**: Microservicio de reportes
  - Endpoint: `POST /reportes/bootcamps` para notificar creación de bootcamp
  - URL configurable mediante variable de entorno `MS_REPORTE_URL` (default: `http://localhost:8084`)
  - Si el endpoint falla, no afecta la creación del bootcamp (solo se registra en logs)

## Configuración de Base de Datos

Levantar MySQL con Docker Compose:

```bash
docker-compose up -d
```

La base de datos se inicializará automáticamente con las tablas necesarias.

## Variables de Entorno

- `MS_CAPACIDAD_URL`: URL del microservicio ms-capacidad (default: `http://localhost:8081`)
- `MS_REPORTE_URL`: URL del microservicio ms-reporte (default: `http://localhost:8084`)

## Ejecutar la Aplicación

```bash
./gradlew bootRun
```

La aplicación estará disponible en `http://localhost:8082`

API Documentation: `http://localhost:8082/swagger-ui.html`

