package com.reto.ms_bootcamp.domain.exceptions;

public class BootcampServiceException extends BootcampException {
    public BootcampServiceException(String message) {
        super(message);
    }

    public BootcampServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

