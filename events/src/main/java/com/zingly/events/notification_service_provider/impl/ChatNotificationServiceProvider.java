package com.zingly.events.notification_service_provider.impl;

import static com.zingly.events.common.constants.ErrorConstants.*;
import static com.zingly.events.common.constants.ProcessingConstants.*;
import com.zingly.events.common.exception.ApplicationValidationException;
import com.zingly.events.event.domain.enums.Channel;
import com.zingly.events.notification_service_provider.NotificationServiceProvider;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
/** Validates and identifies CHAT event processing. */
public class ChatNotificationServiceProvider implements NotificationServiceProvider {

    @Override
    /** Returns the channel supported by this handler. */
    public Channel channel() {
        return Channel.CHAT;
    }

    @Override
    /** Validates the channel-specific payload shape. */
    public void validate(JsonNode payload) {
        requireObject(payload);
        JsonNode message = payload.get(MESSAGE_FIELD);
        if (message != null && (!message.isTextual() || message.stringValue().isBlank())) {
            throw new ApplicationValidationException(CHAT_MESSAGE_INVALID);
        }
    }

    @Override
    /** Returns the handler name stored in enrichment metadata. */
    public String handlerName() {
        return CHAT_HANDLER_NAME;
    }

    /** Requires the channel payload to be a JSON object. */
    private void requireObject(JsonNode payload) {
        if (!payload.isObject()) {
            throw new ApplicationValidationException(CHAT_PAYLOAD_INVALID);
        }
    }
}
