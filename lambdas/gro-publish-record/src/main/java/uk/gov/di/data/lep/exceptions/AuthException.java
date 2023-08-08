package uk.gov.di.data.lep.exceptions;

public class AuthException extends RuntimeException{
    public AuthException(String message, Exception e) {
        super(message, e);
    }
}
