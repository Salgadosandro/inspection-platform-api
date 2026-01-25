package com.vectorlabs.exception;


public class MissingRequiredFieldException extends RuntimeException {
    public MissingRequiredFieldException(String message) {
        super(message);
    }
}
