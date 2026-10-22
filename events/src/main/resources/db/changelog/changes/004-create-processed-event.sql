--liquibase formatted sql

--changeset events:004-create-processed-event
CREATE TABLE processed_event (
    event_id VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    customer_sequence BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    outcome VARCHAR(40) NOT NULL,
    original_payload CLOB NOT NULL,
    enriched_payload CLOB NOT NULL,
    tenant_config_version BIGINT NOT NULL,
    preference_version BIGINT,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_processed_event PRIMARY KEY (event_id),
    CONSTRAINT fk_processed_event_inbox
        FOREIGN KEY (event_id) REFERENCES event_inbox (event_id),
    CONSTRAINT fk_processed_event_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant_config (tenant_id),
    CONSTRAINT ck_processed_channel
        CHECK (channel IN ('CHAT', 'EMAIL', 'VOICE')),
    CONSTRAINT ck_processed_outcome
        CHECK (outcome IN ('DELIVERED', 'SUPPRESSED_BY_PREFERENCE')),
    CONSTRAINT ck_processed_sequence CHECK (customer_sequence > 0),
    CONSTRAINT ck_processed_tenant_version CHECK (tenant_config_version > 0)
);

--rollback DROP TABLE processed_event;
