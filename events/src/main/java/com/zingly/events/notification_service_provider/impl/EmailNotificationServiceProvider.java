package com.zingly.events.notification_service_provider.impl;

import static com.zingly.events.common.constants.ErrorConstants.*;
import static com.zingly.events.common.constants.ProcessingConstants.*;
import com.zingly.events.common.exception.ApplicationValidationException;
import com.zingly.events.event.domain.enums.Channel;
import com.zingly.events.notification_service_provider.NotificationServiceProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
/** Validates and identifies EMAIL event processing. */
public class EmailNotificationServiceProvider implements NotificationServiceProvider {

    @Override
    /** Returns the channel supported by this handler. */
    public Channel channel() {
        return Channel.EMAIL;
    }

    @Override
    /** Validates the channel-specific payload shape. */
    public void validate(JsonNode payload) {
        requireObject(payload);
        validateOptionalText(payload, SUBJECT_FIELD);
        validateOptionalText(payload, BODY_FIELD);
    }

    @Override
    /** Returns the handler name stored in enrichment metadata. */
    public String handlerName() {
        return EMAIL_HANDLER_NAME;
    }

    /** Requires the channel payload to be a JSON object. */
    private void requireObject(JsonNode payload) {
        if (!payload.isObject()) {
            throw new ApplicationValidationException(EMAIL_PAYLOAD_INVALID);
        }
    }

    /** Validates an optional text field when it is present. */
    private void validateOptionalText(JsonNode payload, String field) {
        JsonNode value = payload.get(field);
        if (value != null && (!value.isTextual() || value.stringValue().isBlank())) {
            throw new ApplicationValidationException(EMAIL_FIELD_PREFIX + field + NON_EMPTY_STRING_SUFFIX);
        }
    }
}
