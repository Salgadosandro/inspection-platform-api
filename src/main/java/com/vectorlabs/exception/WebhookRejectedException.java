package com.vectorlabs.exception;

public class WebhookRejectedException extends RuntimeException {
    public WebhookRejectedException(String message) {
        super(message);
    }
}
