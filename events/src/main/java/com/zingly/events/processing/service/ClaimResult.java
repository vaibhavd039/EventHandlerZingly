package com.zingly.events.processing.service;

import java.time.Duration;

import com.zingly.events.event.persistence.domain.EventInboxEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
/** Describes whether a customer head was claimed, delayed, blocked, or absent. */
public class ClaimResult {

    /** Defines the possible coordinator or claim decisions. */
    public enum Type {
        /** Represents the claimed value. */
        CLAIMED,
        /** Represents the wait value. */
        WAIT,
        /** Represents the blocked value. */
        BLOCKED,
        /** Represents the nothing value. */
        NOTHING
    }

    private final Type type;
    private final EventInboxEntity event;
    private final Duration delay;

    /** Performs the claimed operation. */
    public static ClaimResult claimed(EventInboxEntity event) {
        return new ClaimResult(Type.CLAIMED, event, Duration.ZERO);
    }

    /** Performs the wait for operation. */
    public static ClaimResult waitFor(Duration delay) {
        return new ClaimResult(Type.WAIT, null, delay);
    }

    /** Performs the blocked operation. */
    public static ClaimResult blocked() {
        return new ClaimResult(Type.BLOCKED, null, Duration.ZERO);
    }

    /** Performs the nothing operation. */
    public static ClaimResult nothing() {
        return new ClaimResult(Type.NOTHING, null, Duration.ZERO);
    }
}
