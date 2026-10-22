package com.zingly.events.event.controller;

import static com.zingly.events.common.constants.ApiConstants.*;
import java.util.UUID;
import com.zingly.events.event.controller.dto.CreateEventRequest;
import com.zingly.events.event.controller.dto.EventAcceptedResponse;
import com.zingly.events.event.controller.dto.EventStatusResponse;
import com.zingly.events.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(EVENTS_PATH)
@Tag(name = EVENTS_TAG, description = EVENTS_TAG_DESCRIPTION)
/** Exposes REST endpoints for event admission and status lookup. */
public class EventController {

    private final EventService eventService;

    /** Creates an event controller with its required state. */
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @Operation(summary = ACCEPT_EVENT_SUMMARY, description = ACCEPT_EVENT_DESCRIPTION)
    /** Validates idempotency, persists a new event, and schedules its customer. */
    ResponseEntity<EventAcceptedResponse> accept(@Valid @RequestBody CreateEventRequest request) {
        EventAcceptedResponse response = eventService.accept(request);
        HttpStatus status = response.isDuplicate() ? HttpStatus.OK : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping(EVENT_ID_PATH)
    @Operation(summary = EVENT_STATUS_SUMMARY, description = EVENT_STATUS_DESCRIPTION)
    /** Returns the status of one accepted event. */
    EventStatusResponse status(@PathVariable UUID eventId) {
        return eventService.getStatus(eventId);
    }
}
