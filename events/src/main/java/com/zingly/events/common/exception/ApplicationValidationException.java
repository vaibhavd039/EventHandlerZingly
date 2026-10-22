package com.zingly.events.common.exception;

/** Signals invalid application input discovered outside declarative bean validation. */
public class ApplicationValidationException extends IllegalArgumentException {

    /** Creates an application validation exception with a message. */
    public ApplicationValidationException(String message) {
        super(message);
    }

    /** Creates an application validation exception with a message and cause. */
    public ApplicationValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
