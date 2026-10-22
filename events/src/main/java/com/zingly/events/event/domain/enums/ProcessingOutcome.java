package com.zingly.events.event.domain.enums;

/** Defines the successful business outcomes of event processing. */
public enum ProcessingOutcome {
    /** Represents the delivered value. */
    DELIVERED,
    /** Represents the suppressed by preference value. */
    SUPPRESSED_BY_PREFERENCE
}
