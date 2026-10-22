package com.zingly.events.event.util;

import static com.zingly.events.common.constants.ErrorConstants.*;
import static com.zingly.events.common.constants.ProcessingConstants.*;
import com.zingly.events.common.exception.ApplicationStateException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import com.zingly.events.event.controller.dto.CreateEventRequest;
import tools.jackson.databind.JsonNode;

/** Creates a stable fingerprint for event idempotency comparisons. */
public final class EventFingerprintUtil {

    /** Prevents instantiation of this stateless utility class. */
    private EventFingerprintUtil() {
    }

    /** Calculates the SHA-256 fingerprint of immutable event content. */
    public static String fingerprint(CreateEventRequest request) {
        StringBuilder canonical = new StringBuilder();
        append(canonical, request.getEventId().toString());
        append(canonical, request.getTenantId().toString());
        append(canonical, request.getCustomerId().toString());
        append(canonical, request.getChannel().name());
        append(canonical, request.getNotificationType());
        appendCanonicalJson(canonical, request.getPayload());
        append(canonical, request.getTimestamp().toString());
        try {
            byte[] digest = MessageDigest.getInstance(SHA_256)
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new ApplicationStateException(SHA_256_UNAVAILABLE, impossible);
        }
    }

    /** Appends JSON in a property-order-independent canonical form. */
    private static void appendCanonicalJson(StringBuilder target, JsonNode node) {
        if (node.isObject()) {
            target.append('{');
            node.propertyNames().stream().sorted().forEach(name -> {
                append(target, name);
                appendCanonicalJson(target, node.get(name));
            });
            target.append('}');
            return;
        }
        if (node.isArray()) {
            target.append('[');
            node.forEach(child -> appendCanonicalJson(target, child));
            target.append(']');
            return;
        }
        append(target, node.toString());
    }

    /** Appends a length-delimited value to canonical fingerprint input. */
    private static void append(StringBuilder target, String value) {
        target.append(value.length()).append(':').append(value).append('|');
    }
}
