package com.zingly.events.processing.restore;

import java.util.Set;

import com.zingly.events.processing.coordinator.CustomerKey;
import lombok.Value;

@Value
/** Carries unresolved backlog and customer lanes rebuilt at startup. */
public class RestoreSnapshot {
    long unresolvedCount;
    Set<CustomerKey> customers;
}
