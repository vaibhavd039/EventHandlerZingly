package com.zingly.events.tenant.service;

import static com.zingly.events.common.constants.ErrorConstants.*;
import java.util.UUID;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;

import com.zingly.events.common.exception.ResourceNotFoundException;
import com.zingly.events.event.domain.enums.Channel;
import com.zingly.events.tenant.persistence.TenantConfigRepository;
import com.zingly.events.tenant.persistence.TenantRepository;
import com.zingly.events.tenant.persistence.domain.TenantConfigEntity;
import com.zingly.events.tenant.persistence.domain.TenantConfigId;
import com.zingly.events.tenant.persistence.domain.TenantEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Creates tenants and manages their per-channel processing configuration. */
@Service
public class TenantConfigService {

    private final TenantRepository tenantRepository;
    private final TenantConfigRepository configRepository;
    private final Clock clock;

    /** Creates a tenant configuration service with its required repositories. */
    public TenantConfigService(
            TenantRepository tenantRepository,
            TenantConfigRepository configRepository,
            Clock clock) {
        this.tenantRepository = tenantRepository;
        this.configRepository = configRepository;
        this.clock = clock;
    }

    /** Creates the tenant and an enabled row for every currently supported channel. */
    @Transactional
    public synchronized void ensureTenant(UUID tenantId) {
        if (tenantRepository.existsById(tenantId)) {
            ensureMissingChannelRows(tenantId);
            return;
        }
        Instant now = Instant.now(clock);
        tenantRepository.saveAndFlush(new TenantEntity(tenantId, now));
        configRepository.saveAllAndFlush(Arrays.stream(Channel.values())
                .map(channel -> new TenantConfigEntity(tenantId, channel, true, now))
                .toList());
    }

    /** Loads the required configuration for one tenant and channel. */
    @Transactional(readOnly = true)
    public TenantConfigEntity getRequired(UUID tenantId, Channel channel) {
        return configRepository.findById(new TenantConfigId(tenantId, channel)).orElseThrow(() ->
                new ResourceNotFoundException(
                        TENANT_CONFIG_NOT_FOUND_PREFIX + tenantId + CHANNEL_MESSAGE_INFIX + channel));
    }

    /** Adds default-enabled rows when a newer application version introduces a channel. */
    private void ensureMissingChannelRows(UUID tenantId) {
        Arrays.stream(Channel.values())
                .filter(channel -> !configRepository.existsById(new TenantConfigId(tenantId, channel)))
                .map(channel -> new TenantConfigEntity(tenantId, channel, true, Instant.now(clock)))
                .forEach(configRepository::save);
        configRepository.flush();
    }
}
