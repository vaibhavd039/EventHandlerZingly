package com.zingly.events.event.domain.enums;

/** Defines the durable processing states of an inbox event. */
public enum EventStatus {
    /** Represents the queued value. */
    QUEUED,
    /** Represents the running value. */
    RUNNING,
    /** Represents the retry wait value. */
    RETRY_WAIT,
    /** Represents the processed value. */
    PROCESSED,
    /** Represents the dlq value. */
    DLQ
}
