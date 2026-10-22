--liquibase formatted sql

--changeset events:005-create-dead-letter
CREATE TABLE dead_letter (
    dlq_id VARCHAR(36) NOT NULL,
    event_id VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    customer_sequence BIGINT NOT NULL,
    cycle_number INTEGER NOT NULL,
    failure_count INTEGER NOT NULL,
    failure_history CLOB NOT NULL,
    last_error VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    replayed_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_dead_letter PRIMARY KEY (dlq_id),
    CONSTRAINT uq_dead_letter_event_cycle UNIQUE (event_id, cycle_number),
    CONSTRAINT fk_dead_letter_event
        FOREIGN KEY (event_id) REFERENCES event_inbox (event_id),
    CONSTRAINT fk_dead_letter_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant_config (tenant_id),
    CONSTRAINT ck_dead_letter_status CHECK (status IN ('ACTIVE', 'REPLAYED')),
    CONSTRAINT ck_dead_letter_sequence CHECK (customer_sequence > 0),
    CONSTRAINT ck_dead_letter_cycle CHECK (cycle_number > 0),
    CONSTRAINT ck_dead_letter_failures CHECK (failure_count > 0),
    CONSTRAINT ck_dead_letter_version CHECK (version >= 0)
);

--rollback DROP TABLE dead_letter;
