package com.zingly.events.customer_preference.persistence;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import com.zingly.events.event.domain.enums.Channel;
import com.zingly.events.customer_preference.persistence.domain.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Provides database access for user preference records. */
public interface UserPreferenceRepository extends JpaRepository<UserPreferenceEntity, UUID> {

    /** Finds by tenant id and customer id and notification type and channel from persistent storage. */
    Optional<UserPreferenceEntity> findByTenantIdAndCustomerIdAndNotificationTypeAndChannel(
            UUID tenantId,
            UUID customerId,
            String notificationType,
            Channel channel);

    /** Finds by tenant id and customer id order by notification type asc channel asc from persistent storage. */
    List<UserPreferenceEntity> findByTenantIdAndCustomerIdOrderByNotificationTypeAscChannelAsc(
            UUID tenantId,
            UUID customerId);
}
