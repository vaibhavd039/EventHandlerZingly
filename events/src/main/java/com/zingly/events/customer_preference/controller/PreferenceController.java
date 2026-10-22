package com.zingly.events.customer_preference.controller;

import static com.zingly.events.common.constants.ApiConstants.*;
import java.util.UUID;
import java.util.List;

import com.zingly.events.customer_preference.controller.dto.UpdatePreferenceRequest;
import com.zingly.events.customer_preference.controller.dto.UserPreferenceResponse;
import com.zingly.events.customer_preference.controller.dto.PreferenceChangeLogResponse;
import com.zingly.events.customer_preference.service.PreferenceChangeLogService;
import com.zingly.events.customer_preference.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PREFERENCES_PATH)
@Tag(name = PREFERENCES_TAG, description = PREFERENCES_TAG_DESCRIPTION)
/** Exposes REST endpoints for updating and listing user preferences. */
public class PreferenceController {

    private final UserPreferenceService preferenceService;
    private final PreferenceChangeLogService changeLogService;

    /** Creates a preference controller with its required state. */
    public PreferenceController(
            UserPreferenceService preferenceService,
            PreferenceChangeLogService changeLogService) {
        this.preferenceService = preferenceService;
        this.changeLogService = changeLogService;
    }

    @PutMapping
    @Operation(summary = UPSERT_PREFERENCE_SUMMARY, description = UPSERT_PREFERENCE_DESCRIPTION)
    /** Creates or updates a user preference and publishes its change. */
    UserPreferenceResponse update(
            @PathVariable UUID tenantId,
            @PathVariable UUID customerId,
            @Valid @RequestBody UpdatePreferenceRequest request) {
        return preferenceService.update(tenantId, customerId, request);
    }

    @GetMapping
    @Operation(summary = LIST_PREFERENCES_SUMMARY, description = LIST_PREFERENCES_DESCRIPTION)
    /** Lists explicitly configured preferences for a tenant customer. */
    List<UserPreferenceResponse> list(
            @PathVariable UUID tenantId,
            @PathVariable UUID customerId) {
        return preferenceService.list(tenantId, customerId);
    }

    @GetMapping(PREFERENCE_CHANGE_LOGS_PATH)
    @Operation(summary = LIST_CHANGE_LOGS_SUMMARY, description = LIST_CHANGE_LOGS_DESCRIPTION)
    /** Lists JSON change logs for one tenant-scoped customer preference. */
    List<PreferenceChangeLogResponse> listChangeLogs(
            @PathVariable UUID tenantId,
            @PathVariable UUID customerId,
            @PathVariable UUID preferenceId) {
        return changeLogService.list(tenantId, customerId, preferenceId);
    }
}
