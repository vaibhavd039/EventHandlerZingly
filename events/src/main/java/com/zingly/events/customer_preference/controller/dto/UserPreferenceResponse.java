package com.zingly.events.customer_preference.controller.dto;

import java.util.UUID;
import java.time.Instant;

import com.zingly.events.event.domain.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** Carries user preference data returned by the API. */
public class UserPreferenceResponse {

    private UUID preferenceId;
    private UUID tenantId;
    private UUID customerId;
    private String notificationType;
    private Channel channel;
    private boolean enabled;
    private boolean explicitlyConfigured;
    private long version;
    private String updatedBy;
    private Instant updatedAt;
}
