--liquibase formatted sql

--changeset events:001-create-tenant-config
CREATE TABLE tenant_config (
    tenant_id VARCHAR(100) NOT NULL,
    config_version BIGINT NOT NULL DEFAULT 1,
    chat_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    voice_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    default_preference_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    config_json CLOB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_tenant_config PRIMARY KEY (tenant_id),
    CONSTRAINT ck_tenant_config_version CHECK (config_version > 0)
);

--rollback DROP TABLE tenant_config;
