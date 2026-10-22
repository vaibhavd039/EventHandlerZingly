package com.zingly.events.common.exception;

/** Signals that a requested application resource does not exist. */
public class ResourceNotFoundException extends RuntimeException {

    /** Creates a resource not found exception with its required state. */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
