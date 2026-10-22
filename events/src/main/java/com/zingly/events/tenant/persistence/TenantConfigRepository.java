package com.zingly.events.tenant.persistence;

import com.zingly.events.tenant.persistence.domain.TenantConfigEntity;
import com.zingly.events.tenant.persistence.domain.TenantConfigId;
import org.springframework.data.jpa.repository.JpaRepository;

/** Provides database access for tenant-channel configuration records. */
public interface TenantConfigRepository extends JpaRepository<TenantConfigEntity, TenantConfigId> {
}
