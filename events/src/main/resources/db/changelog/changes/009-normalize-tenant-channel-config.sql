--liquibase formatted sql

--changeset events:009-normalize-tenant-channel-config
CREATE TABLE tenant_account (
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tenant_account PRIMARY KEY (tenant_id)
);

INSERT INTO tenant_account (tenant_id, created_at, updated_at)
SELECT tenant_id, created_at, updated_at
FROM tenant_config;

ALTER TABLE customer_sequence DROP CONSTRAINT fk_customer_sequence_tenant;
ALTER TABLE event_inbox DROP CONSTRAINT fk_event_inbox_tenant;
ALTER TABLE processed_event DROP CONSTRAINT fk_processed_event_tenant;
ALTER TABLE dead_letter DROP CONSTRAINT fk_dead_letter_tenant;
ALTER TABLE user_preference DROP CONSTRAINT fk_user_preference_tenant;
ALTER TABLE preference_audit DROP CONSTRAINT fk_preference_audit_tenant;

CREATE TABLE tenant_config_v2 (
    tenant_id VARCHAR(100) NOT NULL,
    channel_type VARCHAR(50) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    config_version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tenant_config_v2 PRIMARY KEY (tenant_id, channel_type),
    CONSTRAINT fk_tenant_config_v2_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant_account (tenant_id),
    CONSTRAINT ck_tenant_config_v2_version CHECK (config_version > 0)
);

INSERT INTO tenant_config_v2
    (tenant_id, channel_type, is_enabled, config_version, created_at, updated_at)
SELECT tenant_id, 'CHAT', chat_enabled, config_version, created_at, updated_at
FROM tenant_config;

INSERT INTO tenant_config_v2
    (tenant_id, channel_type, is_enabled, config_version, created_at, updated_at)
SELECT tenant_id, 'EMAIL', email_enabled, config_version, created_at, updated_at
FROM tenant_config;

INSERT INTO tenant_config_v2
    (tenant_id, channel_type, is_enabled, config_version, created_at, updated_at)
SELECT tenant_id, 'VOICE', voice_enabled, config_version, created_at, updated_at
FROM tenant_config;

DROP TABLE tenant_config;
ALTER TABLE tenant_config_v2 RENAME TO tenant_config;

ALTER TABLE customer_sequence ADD CONSTRAINT fk_customer_sequence_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant_account (tenant_id);
ALTER TABLE event_inbox ADD CONSTRAINT fk_event_inbox_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant_account (tenant_id);
ALTER TABLE processed_event ADD CONSTRAINT fk_processed_event_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant_account (tenant_id);
ALTER TABLE dead_letter ADD CONSTRAINT fk_dead_letter_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant_account (tenant_id);
ALTER TABLE user_preference ADD CONSTRAINT fk_user_preference_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant_account (tenant_id);
ALTER TABLE preference_audit ADD CONSTRAINT fk_preference_audit_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenant_account (tenant_id);

ALTER TABLE event_inbox DROP CONSTRAINT ck_event_channel;
ALTER TABLE processed_event DROP CONSTRAINT ck_processed_channel;
ALTER TABLE user_preference DROP CONSTRAINT ck_user_preference_channel;
ALTER TABLE preference_audit DROP CONSTRAINT ck_preference_audit_channel;

--rollback ALTER TABLE preference_audit DROP CONSTRAINT fk_preference_audit_tenant;
--rollback ALTER TABLE user_preference DROP CONSTRAINT fk_user_preference_tenant;
--rollback ALTER TABLE dead_letter DROP CONSTRAINT fk_dead_letter_tenant;
--rollback ALTER TABLE processed_event DROP CONSTRAINT fk_processed_event_tenant;
--rollback ALTER TABLE event_inbox DROP CONSTRAINT fk_event_inbox_tenant;
--rollback ALTER TABLE customer_sequence DROP CONSTRAINT fk_customer_sequence_tenant;
--rollback CREATE TABLE tenant_config_v1 (tenant_id VARCHAR(100) NOT NULL, config_version BIGINT NOT NULL DEFAULT 1, chat_enabled BOOLEAN NOT NULL DEFAULT TRUE, email_enabled BOOLEAN NOT NULL DEFAULT TRUE, voice_enabled BOOLEAN NOT NULL DEFAULT TRUE, default_preference_enabled BOOLEAN NOT NULL DEFAULT TRUE, config_json CLOB, created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, CONSTRAINT pk_tenant_config_v1 PRIMARY KEY (tenant_id), CONSTRAINT ck_tenant_config_v1_version CHECK (config_version > 0));
--rollback INSERT INTO tenant_config_v1 (tenant_id, config_version, chat_enabled, email_enabled, voice_enabled, default_preference_enabled, config_json, created_at, updated_at) SELECT tenant_id, MAX(config_version), MAX(CASE WHEN channel_type = 'CHAT' THEN is_enabled ELSE FALSE END), MAX(CASE WHEN channel_type = 'EMAIL' THEN is_enabled ELSE FALSE END), MAX(CASE WHEN channel_type = 'VOICE' THEN is_enabled ELSE FALSE END), TRUE, NULL, MIN(created_at), MAX(updated_at) FROM tenant_config GROUP BY tenant_id;
--rollback DROP TABLE tenant_config;
--rollback ALTER TABLE tenant_config_v1 RENAME TO tenant_config;
--rollback ALTER TABLE customer_sequence ADD CONSTRAINT fk_customer_sequence_tenant FOREIGN KEY (tenant_id) REFERENCES tenant_config (tenant_id);
--rollback ALTER TABLE event_inbox ADD CONSTRAINT fk_event_inbox_tenant FOREIGN KEY (tenant_id) REFERENCES tenant_config (tenant_id);
--rollback ALTER TABLE processed_event ADD CONSTRAINT fk_processed_event_tenant FOREIGN KEY (tenant_id) REFERENCES tenant_config (tenant_id);
--rollback ALTER TABLE dead_letter ADD CONSTRAINT fk_dead_letter_tenant FOREIGN KEY (tenant_id) REFERENCES tenant_config (tenant_id);
--rollback ALTER TABLE user_preference ADD CONSTRAINT fk_user_preference_tenant FOREIGN KEY (tenant_id) REFERENCES tenant_config (tenant_id);
--rollback ALTER TABLE preference_audit ADD CONSTRAINT fk_preference_audit_tenant FOREIGN KEY (tenant_id) REFERENCES tenant_config (tenant_id);
--rollback DROP TABLE tenant_account;
--rollback ALTER TABLE event_inbox ADD CONSTRAINT ck_event_channel CHECK (channel IN ('CHAT', 'EMAIL', 'VOICE'));
--rollback ALTER TABLE processed_event ADD CONSTRAINT ck_processed_channel CHECK (channel IN ('CHAT', 'EMAIL', 'VOICE'));
--rollback ALTER TABLE user_preference ADD CONSTRAINT ck_user_preference_channel CHECK (channel IN ('CHAT', 'EMAIL', 'VOICE'));
--rollback ALTER TABLE preference_audit ADD CONSTRAINT ck_preference_audit_channel CHECK (channel IN ('CHAT', 'EMAIL', 'VOICE'));
