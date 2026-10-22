package com.zingly.events.customer_preference.controller.dto;

import com.zingly.events.event.domain.enums.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/** Carries and validates update preference API input. */
public class UpdatePreferenceRequest {

    @NotBlank
    @Size(max = 100)
    private String notificationType;

    @NotNull
    private Channel channel;

    @NotNull
    private Boolean enabled;

    @NotBlank
    @Size(max = 100)
    private String updatedBy;
}
