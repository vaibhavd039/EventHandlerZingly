package com.zingly.events.event.controller.dto;

import java.util.UUID;
import java.time.Instant;

import com.zingly.events.event.domain.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** Carries event status data returned by the API. */
public class EventStatusResponse {

    private UUID eventId;
    private EventStatus status;
    private long customerSequence;
    private int attemptStarts;
    private int cycleFailures;
    private Instant nextAttemptAt;
    private String lastError;
    private Instant statusUpdatedAt;
}
