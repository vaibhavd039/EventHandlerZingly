package com.zingly.events.common.exception;

/** Represents an intentionally simulated processing failure. */
public class SimulatedTransientFailureException extends RuntimeException {

    /** Creates a simulated transient failure exception with its required state. */
    public SimulatedTransientFailureException(String message) {
        super(message);
    }
}
