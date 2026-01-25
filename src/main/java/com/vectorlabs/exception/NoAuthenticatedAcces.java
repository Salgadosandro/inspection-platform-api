package com.vectorlabs.exception;

public class NoAuthenticatedAcces extends RuntimeException {
    public NoAuthenticatedAcces(String message) {
        super(message);
    }
}