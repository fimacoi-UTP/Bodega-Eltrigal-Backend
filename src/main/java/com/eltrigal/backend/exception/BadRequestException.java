package com.eltrigal.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando los datos de una solicitud no cumplen con las reglas de negocio.
 * Retorna automáticamente un código de estado HTTP 400 (Bad Request).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

}
