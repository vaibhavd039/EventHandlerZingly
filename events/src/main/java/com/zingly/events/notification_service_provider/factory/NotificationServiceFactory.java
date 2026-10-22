package com.zingly.events.notification_service_provider.factory;

import static com.zingly.events.common.constants.ErrorConstants.*;
import com.zingly.events.common.exception.ApplicationStateException;
import com.zingly.events.common.exception.ApplicationValidationException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.zingly.events.event.domain.enums.Channel;
import com.zingly.events.notification_service_provider.NotificationServiceProvider;
import org.springframework.stereotype.Component;

@Component
/** Selects the correct channel handler for an event. */
public class NotificationServiceFactory {

    private final Map<Channel, NotificationServiceProvider> handlers = new EnumMap<>(Channel.class);

    /** Creates a notification handler factory with its required state. */
    public NotificationServiceFactory(List<NotificationServiceProvider> notificationHandlers) {
        notificationHandlers.forEach(handler -> register(handler));
        for (Channel channel : Channel.values()) {
            if (!handlers.containsKey(channel)) {
                throw new ApplicationStateException(MISSING_HANDLER_PREFIX + channel);
            }
        }
    }

    /** Returns the notification handler responsible for the requested channel. */
    public NotificationServiceProvider getHandler(Channel channel) {
        NotificationServiceProvider handler = handlers.get(Objects.requireNonNull(channel, CHANNEL_NOT_NULL));
        if (handler == null) {
            throw new ApplicationValidationException(NO_HANDLER_PREFIX + channel);
        }
        return handler;
    }

    /** Registers one handler and rejects ambiguous duplicate channel implementations. */
    private void register(NotificationServiceProvider handler) {
        Objects.requireNonNull(handler, HANDLER_NOT_NULL);
        NotificationServiceProvider previous = handlers.put(handler.channel(), handler);
        if (previous != null) {
            throw new ApplicationStateException(MULTIPLE_HANDLERS_PREFIX + handler.channel());
        }
    }
}
