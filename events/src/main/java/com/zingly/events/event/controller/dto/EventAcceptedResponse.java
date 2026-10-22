package com.zingly.events.event.controller.dto;

import java.util.UUID;
import com.zingly.events.event.domain.enums.EventStatus;
import com.zingly.events.event.persistence.domain.EventInboxEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** Carries event accepted data returned by the API. */
public class EventAcceptedResponse {

    private UUID eventId;
    private EventStatus status;
    private long customerSequence;
    private boolean duplicate;

    /** Creates an API response from a persisted inbox event and duplicate status. */
    public static EventAcceptedResponse from(EventInboxEntity event, boolean duplicate) {
        return EventAcceptedResponse.builder()
                .eventId(event.getEventId())
                .status(event.getStatus())
                .customerSequence(event.getCustomerSequence())
                .duplicate(duplicate)
                .build();
    }
}
