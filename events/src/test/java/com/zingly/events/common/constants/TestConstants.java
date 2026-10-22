package com.zingly.events.common.constants;

/** Defines test configuration, identifiers, payloads, and assertion text. */
public interface TestConstants {

    String TEST_DATASOURCE_PROPERTY = "spring.datasource.url=jdbc:h2:mem:events-integration";
    String DISABLE_H2_CONSOLE_PROPERTY = "spring.h2.console.enabled=false";
    String RETRY_INITIAL_DELAY_PROPERTY = "app.processing.retry.initial-delay=10ms";
    String RETRY_MAX_DELAY_PROPERTY = "app.processing.retry.max-delay=20ms";
    String RETRY_JITTER_PROPERTY = "app.processing.retry.jitter-ratio=0";
    String TRANSIENT_FAILURE_RATE_PROPERTY = "app.processing.retry.transient-failure-rate=0";
    String RECONCILIATION_INTERVAL_PROPERTY = "app.processing.restore.reconciliation-interval=100ms";
    String STALE_RUNNING_THRESHOLD_PROPERTY = "app.processing.restore.stale-running-threshold=100ms";
    String TENANT_UUID = "00000000-0000-0000-0000-000000000001";
    String CUSTOMER_ONE_UUID = "00000000-0000-0000-0000-000000000101";
    String CUSTOMER_TWO_UUID = "00000000-0000-0000-0000-000000000102";
    String EVENT_ONE_UUID = "00000000-0000-0000-0000-000000000201";
    String EVENT_TWO_UUID = "00000000-0000-0000-0000-000000000202";
    String POISON_EVENT_UUID = "00000000-0000-0000-0000-000000000203";
    String ORDER_STATUS_NOTIFICATION = "ORDER_STATUS";
    String INTEGRATION_TEST_ACTOR = "integration-test";
    String INITIAL_CHANGE_LOG = "{\"prevValue\":null,\"currValue\":false}";
    String PREVIOUS_VALUE_FIELD = "prevValue";
    String CURRENT_VALUE_FIELD = "currValue";
    String CHAT_PAYLOAD = "{\"message\":\"hello\"}";
    String EMAIL_PAYLOAD = "{\"body\":\"status\"}";
    String CHANGED_CHAT_PAYLOAD = "{\"message\":\"changed\"}";
    String POISON_VOICE_PAYLOAD = "{\"transcript\":\"call\",\"simulateFailure\":\"ALWAYS\"}";
    String EVENT_TIME = "2026-10-17T09:30:00Z";
    String TIMEOUT_ASSERTION_PREFIX = "Condition was not met within ";
}
