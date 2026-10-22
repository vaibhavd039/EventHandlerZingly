package com.zingly.events.notification_service_provider.impl;

import static com.zingly.events.common.constants.ErrorConstants.*;
import static com.zingly.events.common.constants.ProcessingConstants.*;
import com.zingly.events.common.exception.ApplicationValidationException;
import com.zingly.events.event.domain.enums.Channel;
import com.zingly.events.notification_service_provider.NotificationServiceProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
/** Validates and identifies VOICE event processing. */
public class VoiceNotificationServiceProvider implements NotificationServiceProvider {

    @Override
    /** Returns the channel supported by this handler. */
    public Channel channel() {
        return Channel.VOICE;
    }

    @Override
    /** Validates the channel-specific payload shape. */
    public void validate(JsonNode payload) {
        if (!payload.isObject()) {
            throw new ApplicationValidationException(VOICE_PAYLOAD_INVALID);
        }
        JsonNode transcript = payload.get(TRANSCRIPT_FIELD);
        if (transcript != null && (!transcript.isTextual() || transcript.stringValue().isBlank())) {
            throw new ApplicationValidationException(VOICE_TRANSCRIPT_INVALID);
        }
    }

    @Override
    /** Returns the handler name stored in enrichment metadata. */
    public String handlerName() {
        return VOICE_HANDLER_NAME;
    }
}
