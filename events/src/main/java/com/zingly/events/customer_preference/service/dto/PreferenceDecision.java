package com.zingly.events.customer_preference.service.dto;

import lombok.Value;

@Value
/** Carries the effective preference and its source version. */
public class PreferenceDecision {
    boolean enabled;
    Long version;
    boolean explicitlyConfigured;
}
