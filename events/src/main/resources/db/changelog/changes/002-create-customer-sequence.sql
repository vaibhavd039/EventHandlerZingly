--liquibase formatted sql

--changeset events:002-create-customer-sequence
CREATE TABLE customer_sequence (
    tenant_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    next_sequence BIGINT NOT NULL DEFAULT 1,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_customer_sequence PRIMARY KEY (tenant_id, customer_id),
    CONSTRAINT fk_customer_sequence_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant_config (tenant_id),
    CONSTRAINT ck_customer_next_sequence CHECK (next_sequence > 0)
);

--rollback DROP TABLE customer_sequence;
