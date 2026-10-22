package com.zingly.events.customer_preference.persistence;

import java.util.UUID;
import com.zingly.events.customer_preference.persistence.domain.PreferenceChangeLogEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Provides database access for preference change log records. */
public interface PreferenceChangeLogRepository extends JpaRepository<PreferenceChangeLogEntity, UUID> {

    /** Finds a preference's change logs within its tenant and customer scope. */
    List<PreferenceChangeLogEntity> findByPreferenceIdAndTenantIdAndCustomerIdOrderByChangedAtDesc(
            UUID preferenceId,
            UUID tenantId,
            UUID customerId);
}
