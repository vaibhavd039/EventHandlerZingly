package com.zingly.events.dlq.controller.dto;

import java.util.UUID;
import java.time.Instant;

import com.zingly.events.dlq.DeadLetterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** Carries dead letter data returned by the API. */
public class DeadLetterResponse {

    private UUID dlqId;
    private UUID eventId;
    private UUID tenantId;
    private UUID customerId;
    private long customerSequence;
    private int cycleNumber;
    private int failureCount;
    private String failureHistory;
    private String lastError;
    private DeadLetterStatus status;
    private Instant createdAt;
    private Instant replayedAt;
}
