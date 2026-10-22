package com.zingly.events.event.util;

import static com.zingly.events.common.constants.ErrorConstants.*;

import com.zingly.events.common.exception.ApplicationStateException;
import com.zingly.events.common.exception.ConflictException;
import com.zingly.events.event.controller.dto.CreateEventRequest;
import com.zingly.events.event.controller.dto.EventAcceptedResponse;
import com.zingly.events.event.persistence.domain.EventInboxEntity;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
/** Validates that a repeated event ID carries content identical to the stored event. */
public class EventDuplicateValidator {

    private final ObjectMapper objectMapper;

    /** Creates a duplicate validator with the JSON mapper used for payload comparison. */
    public EventDuplicateValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** Validates duplicate equivalence and returns the existing event response. */
    public EventAcceptedResponse validateAndCreateResponse(
            EventInboxEntity existing,
            String fingerprint,
            CreateEventRequest request) {
        boolean exactMatch = existing.getContentFingerprint().equals(fingerprint)
                && existing.getTenantId().equals(request.getTenantId())
                && existing.getCustomerId().equals(request.getCustomerId())
                && existing.getChannel() == request.getChannel()
                && existing.getNotificationType().equals(request.getNotificationType())
                && existing.getEventTimestamp().equals(request.getTimestamp());
        try {
            exactMatch = exactMatch
                    && objectMapper.readTree(existing.getPayload()).equals(request.getPayload());
        } catch (JacksonException invalidStoredPayload) {
            throw new ApplicationStateException(STORED_PAYLOAD_INVALID, invalidStoredPayload);
        }
        if (!exactMatch) {
            throw new ConflictException(DUPLICATE_EVENT_CONFLICT);
        }
        return EventAcceptedResponse.from(existing, true);
    }
}
