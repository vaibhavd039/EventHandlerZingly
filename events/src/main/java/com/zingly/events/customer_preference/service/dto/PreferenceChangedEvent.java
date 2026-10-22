package com.zingly.events.customer_preference.service.dto;

import java.util.UUID;
import java.time.Instant;

import com.zingly.events.event.domain.enums.Channel;
import lombok.Value;

@Value
/** Describes a committed preference change for observer notification. */
public class PreferenceChangedEvent {
    UUID preferenceId;
    UUID tenantId;
    UUID customerId;
    String notificationType;
    Channel channel;
    Boolean oldEnabled;
    boolean newEnabled;
    long preferenceVersion;
    String changedBy;
    Instant changedAt;

    /** Builds the cache key represented by this preference change. */
    public PreferenceKey key() {
        return new PreferenceKey(tenantId, customerId, notificationType, channel);
    }
}
