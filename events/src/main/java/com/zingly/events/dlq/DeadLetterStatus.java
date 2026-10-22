package com.zingly.events.dlq;

/** Defines the lifecycle states of a dead-letter record. */
public enum DeadLetterStatus {
    /** Represents the active value. */
    ACTIVE,
    /** Represents the replayed value. */
    REPLAYED
}
