package com.zingly.events.customer_preference.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.zingly.events.customer_preference.service.dto.PreferenceDecision;
import com.zingly.events.customer_preference.service.dto.PreferenceKey;
import org.springframework.stereotype.Component;

@Component
/** Caches explicitly configured notification decisions in memory. */
public class PreferenceCache {

    private final ConcurrentMap<PreferenceKey, PreferenceDecision> decisions = new ConcurrentHashMap<>();

    /** Performs the get operation. */
    public Optional<PreferenceDecision> get(PreferenceKey key) {
        return Optional.ofNullable(decisions.get(key));
    }

    /** Stores or replaces a cached preference decision. */
    public void put(PreferenceKey key, PreferenceDecision decision) {
        decisions.put(key, decision);
    }
}
