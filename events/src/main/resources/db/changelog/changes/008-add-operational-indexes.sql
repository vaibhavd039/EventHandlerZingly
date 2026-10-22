--liquibase formatted sql

--changeset events:008-01-index-event-head
CREATE INDEX idx_event_inbox_customer_head
    ON event_inbox (tenant_id, customer_id, status, customer_sequence);
--rollback DROP INDEX idx_event_inbox_customer_head;

--changeset events:008-02-index-retry-due
CREATE INDEX idx_event_inbox_retry_due
    ON event_inbox (status, next_attempt_at);
--rollback DROP INDEX idx_event_inbox_retry_due;

--changeset events:008-03-index-processed-channel-time
CREATE INDEX idx_processed_event_channel_time
    ON processed_event (channel, processed_at);
--rollback DROP INDEX idx_processed_event_channel_time;

--changeset events:008-04-index-dlq-active
CREATE INDEX idx_dead_letter_status_created
    ON dead_letter (status, created_at);
--rollback DROP INDEX idx_dead_letter_status_created;

--changeset events:008-05-index-preference-lookup
CREATE INDEX idx_user_preference_lookup
    ON user_preference (tenant_id, customer_id, notification_type, channel, enabled);
--rollback DROP INDEX idx_user_preference_lookup;

--changeset events:008-06-index-preference-audit
CREATE INDEX idx_preference_audit_preference_time
    ON preference_audit (preference_id, changed_at);
--rollback DROP INDEX idx_preference_audit_preference_time;
