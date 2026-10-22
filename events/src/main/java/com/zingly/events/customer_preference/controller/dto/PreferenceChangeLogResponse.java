package com.zingly.events.customer_preference.controller.dto;

import java.time.Instant;
import java.util.UUID;

import com.zingly.events.event.domain.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** Carries a preference change log while exposing its stored string as JSON. */
public class PreferenceChangeLogResponse {

    private UUID changeLogId;
    private UUID preferenceId;
    private UUID tenantId;
    private UUID customerId;
    private String notificationType;
    private Channel channel;
    private JsonNode changeLog;
    private long preferenceVersion;
    private String changedBy;
    private Instant changedAt;
}
