package com.zingly.events.customer_preference.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.zingly.events.customer_preference.service.dto.PreferenceChangedEvent;
import com.zingly.events.customer_preference.service.dto.PreferenceDecision;
import com.zingly.events.customer_preference.service.dto.PreferenceKey;
import com.zingly.events.event.domain.enums.Channel;
import com.zingly.events.customer_preference.controller.dto.UpdatePreferenceRequest;
import com.zingly.events.customer_preference.controller.dto.UserPreferenceResponse;
import com.zingly.events.customer_preference.persistence.UserPreferenceRepository;
import com.zingly.events.customer_preference.persistence.domain.UserPreferenceEntity;
import com.zingly.events.tenant.persistence.domain.TenantConfigEntity;
import com.zingly.events.tenant.service.TenantConfigService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/** Updates subscriptions and resolves effective notification preferences. */
public class UserPreferenceService {

    private final UserPreferenceRepository repository;
    private final TenantConfigService tenantConfigService;
    private final PreferenceCache cache;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    /** Creates a user preference service with its required state. */
    public UserPreferenceService(
            UserPreferenceRepository repository,
            TenantConfigService tenantConfigService,
            PreferenceCache cache,
            ApplicationEventPublisher eventPublisher,
            Clock clock) {
        this.repository = repository;
        this.tenantConfigService = tenantConfigService;
        this.cache = cache;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Transactional
    /** Creates or updates a user preference and publishes its change. */
    public UserPreferenceResponse update(
            UUID tenantId,
            UUID customerId,
            UpdatePreferenceRequest request) {
        tenantConfigService.ensureTenant(tenantId);
        Instant now = Instant.now(clock);
        UserPreferenceEntity preference = repository
                .findByTenantIdAndCustomerIdAndNotificationTypeAndChannel(
                        tenantId,
                        customerId,
                        request.getNotificationType(),
                        request.getChannel())
                .orElse(null);
        Boolean oldEnabled = preference == null ? null : preference.isEnabled();
        if (preference == null) {
            preference = new UserPreferenceEntity(
                    UUID.randomUUID(),
                    tenantId,
                    customerId,
                    request.getNotificationType(),
                    request.getChannel(),
                    request.getEnabled(),
                    request.getUpdatedBy(),
                    now);
        } else {
            preference.update(request.getEnabled(), request.getUpdatedBy(), now);
        }
        preference = repository.saveAndFlush(preference);

        eventPublisher.publishEvent(new PreferenceChangedEvent(
                preference.getPreferenceId(),
                tenantId,
                customerId,
                request.getNotificationType(),
                request.getChannel(),
                oldEnabled,
                preference.isEnabled(),
                preference.getVersion(),
                request.getUpdatedBy(),
                now));
        return toResponse(preference, true);
    }

    @Transactional(readOnly = true)
    /** Lists explicitly configured preferences for a tenant customer. */
    public List<UserPreferenceResponse> list(UUID tenantId, UUID customerId) {
        return repository.findByTenantIdAndCustomerIdOrderByNotificationTypeAscChannelAsc(
                        tenantId, customerId)
                .stream()
                .map(preference -> toResponse(preference, true))
                .toList();
    }

    @Transactional(readOnly = true)
    /** Resolves an explicit preference or falls back to tenant channel enablement. */
    public PreferenceDecision resolve(
            UUID tenantId,
            UUID customerId,
            String notificationType,
            Channel channel,
            TenantConfigEntity tenantConfig) {
        PreferenceKey key = new PreferenceKey(tenantId, customerId, notificationType, channel);
        return cache.get(key).orElseGet(() -> {
            PreferenceDecision decision = repository
                    .findByTenantIdAndCustomerIdAndNotificationTypeAndChannel(
                            tenantId, customerId, notificationType, channel)
                    .map(preference -> new PreferenceDecision(
                            preference.isEnabled(), preference.getVersion(), true))
                    .orElseGet(() -> new PreferenceDecision(
                            tenantConfig.isEnabled(), null, false));
            if (decision.isExplicitlyConfigured()) {
                cache.put(key, decision);
            }
            return decision;
        });
    }

    /** Maps a persistence entity to its API response. */
    private UserPreferenceResponse toResponse(
            UserPreferenceEntity preference,
            boolean explicitlyConfigured) {
        return UserPreferenceResponse.builder()
                .preferenceId(preference.getPreferenceId())
                .tenantId(preference.getTenantId())
                .customerId(preference.getCustomerId())
                .notificationType(preference.getNotificationType())
                .channel(preference.getChannel())
                .enabled(preference.isEnabled())
                .explicitlyConfigured(explicitlyConfigured)
                .version(preference.getVersion())
                .updatedBy(preference.getUpdatedBy())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }
}
