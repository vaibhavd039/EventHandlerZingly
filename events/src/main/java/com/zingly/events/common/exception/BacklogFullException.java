package com.zingly.events.common.exception;

/** Signals that the unresolved-event capacity has been exhausted. */
public class BacklogFullException extends RuntimeException {

    /** Creates a backlog full exception with its required state. */
    public BacklogFullException(String message) {
        super(message);
    }
}
