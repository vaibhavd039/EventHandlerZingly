package com.zingly.events.event.controller.dto;

import java.util.UUID;
import java.time.Instant;

import com.zingly.events.event.domain.enums.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** Carries and validates create event API input. */
public class CreateEventRequest {

    @NotNull
    private UUID eventId;

    @NotNull
    private UUID tenantId;

    @NotNull
    private UUID customerId;

    @NotNull
    private Channel channel;

    @NotBlank
    @Size(max = 100)
    private String notificationType;

    @NotNull
    private JsonNode payload;

    @NotNull
    private Instant timestamp;
}
