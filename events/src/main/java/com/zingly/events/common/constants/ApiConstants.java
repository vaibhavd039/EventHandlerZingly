package com.zingly.events.common.constants;

/** Defines REST routes and OpenAPI text shared by controllers and configuration. */
public interface ApiConstants {

    String EVENTS_PATH = "/events";
    String EVENT_ID_PATH = "/{eventId}";
    String DLQ_PATH = "/dlq";
    String DLQ_REPLAY_PATH = "/{dlqId}/replay";
    String STATS_PATH = "/stats";
    String PREFERENCES_PATH = "/tenants/{tenantId}/users/{customerId}/preferences";
    String PREFERENCE_CHANGE_LOGS_PATH = "/{preferenceId}/change-logs";
    String EVENTS_TAG = "Events";
    String EVENTS_TAG_DESCRIPTION = "Accept engagement events and inspect their processing status";
    String ACCEPT_EVENT_SUMMARY = "Accept an engagement event";
    String ACCEPT_EVENT_DESCRIPTION = "Persists the event before returning and deduplicates by eventId.";
    String EVENT_STATUS_SUMMARY = "Get event status";
    String EVENT_STATUS_DESCRIPTION = "Returns attempts, retry timing, error details, and current durable status.";
    String DLQ_TAG = "Dead-letter queue";
    String DLQ_TAG_DESCRIPTION = "Inspect failed events and replay blocked customer heads";
    String LIST_DLQ_SUMMARY = "List dead letters";
    String LIST_DLQ_DESCRIPTION = "Lists active dead letters by default and can include replay history.";
    String REPLAY_DLQ_SUMMARY = "Replay a dead letter";
    String REPLAY_DLQ_DESCRIPTION = "Resets the original event with a fresh three-failure retry budget.";
    String PREFERENCES_TAG = "Preferences";
    String PREFERENCES_TAG_DESCRIPTION = "Manage tenant-scoped customer notification subscriptions";
    String UPSERT_PREFERENCE_SUMMARY = "Upsert a user preference";
    String UPSERT_PREFERENCE_DESCRIPTION = "Updates one notification type and channel subscription, then notifies observers.";
    String LIST_PREFERENCES_SUMMARY = "List user preferences";
    String LIST_PREFERENCES_DESCRIPTION = "Returns explicitly configured preferences for the tenant customer.";
    String LIST_CHANGE_LOGS_SUMMARY = "List preference change logs";
    String LIST_CHANGE_LOGS_DESCRIPTION = "Returns stored change logs with the changeLog field represented as JSON.";
    String STATS_TAG = "Statistics";
    String STATS_TAG_DESCRIPTION = "Inspect processing counts and per-channel throughput";
    String STATS_SUMMARY = "Get processing statistics";
    String STATS_DESCRIPTION = "Returns durable counters plus current in-flight and recovery gauges.";
    String DEFAULT_PAGE = "0";
    String DEFAULT_PAGE_SIZE = "50";
    String DEFAULT_FALSE = "false";
    String OPEN_API_TITLE = "Engagement Event Processing API";
    String OPEN_API_VERSION = "1.0.0";
    String OPEN_API_DESCRIPTION = "Ordered, idempotent event processing with retries, DLQ replay, preferences, and statistics.";
    String OPEN_API_CONTACT_NAME = "Zingly Engineering";
    String OPEN_API_CONTACT_URL = "Educational Project";
    String LOCAL_SERVER_URL = "http://localhost:8080";
    String LOCAL_SERVER_DESCRIPTION = "Local development server";
}
