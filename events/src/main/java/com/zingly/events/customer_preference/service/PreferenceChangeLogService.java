package com.zingly.events.customer_preference.service;

import static com.zingly.events.common.constants.ErrorConstants.*;
import com.zingly.events.common.exception.ApplicationStateException;
import java.util.List;
import java.util.UUID;

import com.zingly.events.customer_preference.controller.dto.PreferenceChangeLogResponse;
import com.zingly.events.customer_preference.persistence.PreferenceChangeLogRepository;
import com.zingly.events.customer_preference.persistence.domain.PreferenceChangeLogEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
/** Reads stored preference change logs and exposes their serialized content as JSON. */
public class PreferenceChangeLogService {

    private final PreferenceChangeLogRepository repository;
    private final ObjectMapper objectMapper;

    /** Creates a preference change log service with its required dependencies. */
    public PreferenceChangeLogService(
            PreferenceChangeLogRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    /** Lists change logs for one tenant-scoped customer preference. */
    public List<PreferenceChangeLogResponse> list(
            UUID tenantId,
            UUID customerId,
            UUID preferenceId) {
        return repository
                .findByPreferenceIdAndTenantIdAndCustomerIdOrderByChangedAtDesc(
                        preferenceId, tenantId, customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Maps a stored VARCHAR JSON document into the API response JSON field. */
    private PreferenceChangeLogResponse toResponse(PreferenceChangeLogEntity entity) {
        try {
            return PreferenceChangeLogResponse.builder()
                    .changeLogId(entity.getChangeLogId())
                    .preferenceId(entity.getPreferenceId())
                    .tenantId(entity.getTenantId())
                    .customerId(entity.getCustomerId())
                    .notificationType(entity.getNotificationType())
                    .channel(entity.getChannel())
                    .changeLog(objectMapper.readTree(entity.getChangeLog()))
                    .preferenceVersion(entity.getPreferenceVersion())
                    .changedBy(entity.getChangedBy())
                    .changedAt(entity.getChangedAt())
                    .build();
        } catch (JacksonException invalidChangeLog) {
            throw new ApplicationStateException(CHANGE_LOG_INVALID, invalidChangeLog);
        }
    }
}
