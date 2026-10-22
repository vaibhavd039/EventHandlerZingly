--liquibase formatted sql

--changeset events:006-create-user-preference
CREATE TABLE user_preference (
    preference_id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL,
    CONSTRAINT pk_user_preference PRIMARY KEY (preference_id),
    CONSTRAINT uq_user_preference_scope
        UNIQUE (tenant_id, customer_id, notification_type, channel),
    CONSTRAINT fk_user_preference_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant_config (tenant_id),
    CONSTRAINT ck_user_preference_channel
        CHECK (channel IN ('CHAT', 'EMAIL', 'VOICE')),
    CONSTRAINT ck_user_preference_version CHECK (version >= 0)
);

--rollback DROP TABLE user_preference;
