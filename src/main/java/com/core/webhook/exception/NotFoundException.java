package com.core.webhook.exception;

public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable ex) {
        super(message, ex);
    }

}
