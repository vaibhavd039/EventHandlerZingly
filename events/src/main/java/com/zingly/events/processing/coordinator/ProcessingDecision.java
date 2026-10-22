package com.zingly.events.processing.coordinator;

import java.time.Duration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
/** Tells the coordinator how to schedule a customer after one worker pass. */
public class ProcessingDecision {

    /** Defines the possible coordinator or claim decisions. */
    public enum Type {
        /** Represents the advance value. */
        ADVANCE,
        /** Represents the wait value. */
        WAIT,
        /** Represents the blocked value. */
        BLOCKED,
        /** Represents the nothing value. */
        NOTHING
    }

    private final Type type;
    private final Duration delay;

    /** Performs the advance operation. */
    public static ProcessingDecision advance() {
        return new ProcessingDecision(Type.ADVANCE, Duration.ZERO);
    }

    /** Performs the wait for operation. */
    public static ProcessingDecision waitFor(Duration delay) {
        return new ProcessingDecision(Type.WAIT, delay.isNegative() ? Duration.ZERO : delay);
    }

    /** Performs the blocked operation. */
    public static ProcessingDecision blocked() {
        return new ProcessingDecision(Type.BLOCKED, Duration.ZERO);
    }

    /** Performs the nothing operation. */
    public static ProcessingDecision nothing() {
        return new ProcessingDecision(Type.NOTHING, Duration.ZERO);
    }
}
