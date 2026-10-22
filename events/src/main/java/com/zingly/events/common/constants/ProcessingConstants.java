package com.zingly.events.common.constants;

/** Defines processing, notification payload, enrichment, and failure-history tokens. */
public interface ProcessingConstants {

    String SHA_256 = "SHA-256";
    String MESSAGE_FIELD = "message";
    String SUBJECT_FIELD = "subject";
    String BODY_FIELD = "body";
    String TRANSCRIPT_FIELD = "transcript";
    String SIMULATE_FAILURE_FIELD = "simulateFailure";
    String ALWAYS_FAILURE = "ALWAYS";
    String CHAT_HANDLER_NAME = "chat-handler";
    String EMAIL_HANDLER_NAME = "email-handler";
    String VOICE_HANDLER_NAME = "voice-handler";
    String ENRICHED_PAYLOAD_FIELD = "payload";
    String ENRICHED_TENANT_ID_FIELD = "tenantId";
    String ENRICHED_TENANT_CONFIG_VERSION_FIELD = "tenantConfigVersion";
    String ENRICHED_HANDLER_FIELD = "handler";
    String ENRICHED_NOTIFICATION_ENABLED_FIELD = "notificationEnabled";
    String ENRICHED_PREFERENCE_VERSION_FIELD = "preferenceVersion";
    String ENRICHED_OUTCOME_FIELD = "outcome";
    String FAILURE_CYCLE_PREFIX = "Cycle ";
    String FAILURE_NUMBER_INFIX = ", failure ";
    String COLON_SEPARATOR = ": ";
}
