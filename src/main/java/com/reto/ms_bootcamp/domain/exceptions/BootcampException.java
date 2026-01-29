package com.reto.ms_bootcamp.domain.exceptions;

public class BootcampException extends RuntimeException {
    public BootcampException(String message) {
        super(message);
    }

    public BootcampException(String message, Throwable cause) {
        super(message, cause);
    }
}

