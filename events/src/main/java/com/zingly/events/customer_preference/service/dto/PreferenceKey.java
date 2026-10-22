package com.zingly.events.customer_preference.service.dto;

import java.util.UUID;
import com.zingly.events.event.domain.enums.Channel;
import lombok.Value;

@Value
/** Identifies one tenant, customer, notification type, and channel preference. */
public class PreferenceKey {
    UUID tenantId;
    UUID customerId;
    String notificationType;
    Channel channel;
}
