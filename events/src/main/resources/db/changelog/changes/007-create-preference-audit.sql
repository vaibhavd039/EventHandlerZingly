--liquibase formatted sql

--changeset events:007-create-preference-audit
CREATE TABLE preference_audit (
    audit_id VARCHAR(36) NOT NULL,
    preference_id VARCHAR(36) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    notification_type VARCHAR(100) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    old_enabled BOOLEAN,
    new_enabled BOOLEAN NOT NULL,
    preference_version BIGINT NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_preference_audit PRIMARY KEY (audit_id),
    CONSTRAINT fk_preference_audit_preference
        FOREIGN KEY (preference_id) REFERENCES user_preference (preference_id),
    CONSTRAINT fk_preference_audit_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenant_config (tenant_id),
    CONSTRAINT ck_preference_audit_channel
        CHECK (channel IN ('CHAT', 'EMAIL', 'VOICE')),
    CONSTRAINT ck_preference_audit_version CHECK (preference_version >= 0)
);

--rollback DROP TABLE preference_audit;
