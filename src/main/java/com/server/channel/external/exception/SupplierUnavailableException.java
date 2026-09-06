package com.server.channel.external.exception;

public class SupplierUnavailableException extends RuntimeException {

    public SupplierUnavailableException(String message) {
        super(message);
    }

    public SupplierUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
