package com.zingly.events.dlq.service.dto;

import java.util.UUID;
import com.zingly.events.event.domain.enums.EventStatus;
import com.zingly.events.processing.coordinator.CustomerKey;
import lombok.Value;

@Value
/** Carries the internal result of a dead-letter replay transaction. */
public class ReplayResult {
    UUID dlqId;
    UUID eventId;
    EventStatus eventStatus;
    boolean alreadyReplayed;
    CustomerKey customerKey;
}
