--liquibase formatted sql

--changeset events:003-create-event-inbox
CREATE TABLE event_inbox (
    event_id VARCHAR(100) NOT NULL,
    content_fingerprint CHAR(64) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    payload CLOB NOT NULL,
    event_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    customer_sequence BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'QUEUED',
    attempt_starts INTEGER NOT NULL DEFAULT 0,
    cycle_failures INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP WITH TIME ZONE,
    claimed_at TIMESTAMP WITH TIME ZONE,
    status_updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_error VARCHAR(2000),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_event_inbox PRIMARY KEY (event_id),
    CONSTRAINT uq_event_customer_sequence
        UNIQUE (tenant_id, customer_id, customer_sequence),
    CONSTRAINT fk_event_inbox_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant_config (tenant_id),
    CONSTRAINT ck_event_channel
        CHECK (channel IN ('CHAT', 'EMAIL', 'VOICE')),
    CONSTRAINT ck_event_status
        CHECK (status IN ('QUEUED', 'RUNNING', 'RETRY_WAIT', 'PROCESSED', 'DLQ')),
    CONSTRAINT ck_event_sequence CHECK (customer_sequence > 0),
    CONSTRAINT ck_event_attempts CHECK (attempt_starts >= 0),
    CONSTRAINT ck_event_cycle_failures CHECK (cycle_failures >= 0),
    CONSTRAINT ck_event_version CHECK (version >= 0)
);

--rollback DROP TABLE event_inbox;
