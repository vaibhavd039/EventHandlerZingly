--liquibase formatted sql

--changeset events:010-rename-preference-change-log
ALTER TABLE preference_audit RENAME TO preference_change_log;
ALTER TABLE preference_change_log RENAME COLUMN audit_id TO change_log_id;

ALTER TABLE preference_change_log ADD COLUMN change_log VARCHAR(1000);
UPDATE preference_change_log
SET change_log = '{"prevValue":'
    || CASE WHEN old_enabled IS NULL THEN 'null' ELSE LOWER(CAST(old_enabled AS VARCHAR)) END
    || ',"currValue":' || LOWER(CAST(new_enabled AS VARCHAR)) || '}';
ALTER TABLE preference_change_log ALTER COLUMN change_log SET NOT NULL;

ALTER TABLE preference_change_log DROP COLUMN old_enabled;
ALTER TABLE preference_change_log DROP COLUMN new_enabled;

DROP INDEX idx_preference_audit_preference_time;
CREATE INDEX idx_preference_change_log_preference_time
    ON preference_change_log (preference_id, changed_at);

--rollback DROP INDEX idx_preference_change_log_preference_time;
--rollback ALTER TABLE preference_change_log ADD COLUMN old_enabled BOOLEAN;
--rollback ALTER TABLE preference_change_log ADD COLUMN new_enabled BOOLEAN;
--rollback UPDATE preference_change_log SET old_enabled = CAST(JSON_VALUE(change_log, '$.prevValue') AS BOOLEAN), new_enabled = CAST(JSON_VALUE(change_log, '$.currValue') AS BOOLEAN);
--rollback ALTER TABLE preference_change_log ALTER COLUMN new_enabled SET NOT NULL;
--rollback ALTER TABLE preference_change_log DROP COLUMN change_log;
--rollback ALTER TABLE preference_change_log RENAME COLUMN change_log_id TO audit_id;
--rollback ALTER TABLE preference_change_log RENAME TO preference_audit;
--rollback CREATE INDEX idx_preference_audit_preference_time ON preference_audit (preference_id, changed_at);
