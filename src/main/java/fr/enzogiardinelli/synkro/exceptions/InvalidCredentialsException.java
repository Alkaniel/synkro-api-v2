package fr.enzogiardinelli.synkro.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException{
    public InvalidCredentialsException() {
        super("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }

    public InvalidCredentialsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
