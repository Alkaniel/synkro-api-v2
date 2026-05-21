package fr.enzogiardinelli.synkro.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a request is valid but in conflict with the current state of the server.
 * e.g : Same email used
 */
public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
