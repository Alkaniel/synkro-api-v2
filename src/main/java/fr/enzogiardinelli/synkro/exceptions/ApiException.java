package fr.enzogiardinelli.synkro.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Mother class of all API exceptions.
 * Each child classes have a HTTP code to return to the client.
 * GlobalExceptionHandler reads getStatus() and produce the response.
 */
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
