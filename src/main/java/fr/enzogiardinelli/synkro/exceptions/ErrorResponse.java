package fr.enzogiardinelli.synkro.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard structure for API's error responses.
 *
 * @param timestamp - when error occured
 * @param status - HTTP code
 * @param error - HTTP label (e.g : "Not found")
 * @param message - clear message for web dev
 * @param path - path to error source
 * @param fields - when there is a validation error
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, Map<String, String> fields) {
    /**
     * Constructor for simple error (w/out validation fields)
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path ) {
        this(timestamp, status, error, message, path, null);
    }
}
