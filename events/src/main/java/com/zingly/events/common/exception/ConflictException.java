package com.zingly.events.common.exception;

/** Signals that a request conflicts with existing persisted state. */
public class ConflictException extends RuntimeException {

    /** Creates a conflict exception with its required state. */
    public ConflictException(String message) {
        super(message);
    }
}
