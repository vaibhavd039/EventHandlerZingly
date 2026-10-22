package com.zingly.events.common.constants;

/** Defines preference change-log JSON and routing tokens. */
public interface PreferenceConstants {

    String NULL_JSON_VALUE = "null";
    String CHANGE_LOG_PREVIOUS_PREFIX = "{\"prevValue\":";
    String CHANGE_LOG_CURRENT_INFIX = ",\"currValue\":";
    String JSON_OBJECT_SUFFIX = "}";
    String ROUTING_KEY_SEPARATOR = "/";
    String ROUTING_REFRESHED_PREFIX = "Routing preference refreshed for ";
}
