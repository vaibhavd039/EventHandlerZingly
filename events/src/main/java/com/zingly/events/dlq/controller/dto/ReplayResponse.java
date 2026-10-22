package com.zingly.events.dlq.controller.dto;

import java.util.UUID;
import com.zingly.events.event.domain.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** Carries replay data returned by the API. */
public class ReplayResponse {

    private UUID dlqId;
    private UUID eventId;
    private EventStatus eventStatus;
    private boolean alreadyReplayed;
}
