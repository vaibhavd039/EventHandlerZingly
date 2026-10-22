package com.zingly.events.common.exception;

/** Signals an unexpected or inconsistent internal application state. */
public class ApplicationStateException extends IllegalStateException {

    /** Creates an application state exception with a message. */
    public ApplicationStateException(String message) {
        super(message);
    }

    /** Creates an application state exception with a message and cause. */
    public ApplicationStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
