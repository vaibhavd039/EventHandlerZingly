package com.zingly.events.notification_service_provider;

import com.zingly.events.event.domain.enums.Channel;
import tools.jackson.databind.JsonNode;

/** Defines channel-specific payload validation and handler metadata. */
public interface NotificationServiceProvider {

    /** Returns the channel supported by this handler. */
    Channel channel();

    /** Validates the channel-specific payload shape. */
    void validate(JsonNode payload);

    /** Returns the handler name stored in enrichment metadata. */
    String handlerName();
}
