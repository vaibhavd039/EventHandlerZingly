package com.zingly.events.processing.restore;

import java.util.Set;

import com.zingly.events.processing.coordinator.CustomerKey;
import lombok.Value;

@Value
/** Carries reconciliation results used to repair runtime scheduling. */
public class ReconcileSnapshot {
    int completedEvents;
    Set<CustomerKey> customers;
}
