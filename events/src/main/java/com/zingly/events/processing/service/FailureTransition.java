package com.zingly.events.processing.service;

import java.time.Duration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
/** Describes whether a failed attempt should retry or enter the DLQ. */
public class FailureTransition {

    private final boolean deadLettered;
    private final Duration retryDelay;

    /** Performs the retry operation. */
    public static FailureTransition retry(Duration delay) {
        return new FailureTransition(false, delay);
    }

    /** Performs the dead lettered operation. */
    public static FailureTransition deadLettered() {
        return new FailureTransition(true, Duration.ZERO);
    }
}
