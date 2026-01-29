CREATE TABLE IF NOT EXISTS bootcamp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE,
    descripcion VARCHAR(90),
    fecha_lanzamiento DATE NOT NULL,
    duracion_semanas INT NOT NULL
);

CREATE TABLE IF NOT EXISTS bootcamp_capacidad (
    bootcamp_id BIGINT NOT NULL,
    capacidad_id BIGINT NOT NULL,
    PRIMARY KEY (bootcamp_id, capacidad_id)
);

