package com.zingly.events.processing.coordinator;

import java.util.UUID;
import lombok.Value;

@Value
/** Identifies one tenant-scoped customer ordering lane. */
public class CustomerKey {
    UUID tenantId;
    UUID customerId;
}
