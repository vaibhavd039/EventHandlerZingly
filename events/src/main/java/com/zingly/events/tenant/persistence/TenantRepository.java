package com.zingly.events.tenant.persistence;

import java.util.UUID;

import com.zingly.events.tenant.persistence.domain.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Provides database access for tenant accounts. */
public interface TenantRepository extends JpaRepository<TenantEntity, UUID> {
}
