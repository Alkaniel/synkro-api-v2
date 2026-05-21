package fr.enzogiardinelli.synkro.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a ressource is not found in the database.
 */
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(resourceName + " not found : " + identifier, HttpStatus.NOT_FOUND);
    }
}
