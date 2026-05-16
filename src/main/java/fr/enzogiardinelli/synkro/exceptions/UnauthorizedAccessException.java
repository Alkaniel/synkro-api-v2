package fr.enzogiardinelli.synkro.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Thrown when user is authenticated but is not authorized to access a resource.
 */
public class UnauthorizedAccessException extends ApiException {
    public UnauthorizedAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
