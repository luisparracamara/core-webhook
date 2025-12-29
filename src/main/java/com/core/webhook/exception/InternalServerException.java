package com.core.webhook.exception;

public class InternalServerException extends DomainException {

    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(String message, Throwable ex) {
        super(message, ex);
    }

}
