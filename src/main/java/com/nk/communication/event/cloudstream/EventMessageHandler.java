package com.nk.communication.event.cloudstream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nk.communication.event.EnvelopedEvent;
import com.nk.communication.event.Event;
import com.nk.communication.event.EventHeader;
import com.nk.communication.event.handler.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EventMessageHandler<T extends Event> implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(EventMessageHandler.class);
    private final Class<T> eventClass;
    private final List<EventMessageFilter> filters;
    private final List<EventHandler<T>> handlers;
    private final ObjectMapper objectMapper;

    EventMessageHandler(Class<T> eventClass, List<EventMessageFilter> filters, List<EventHandler<T>> handlers, ObjectMapper objectMapper) {
        this.eventClass = eventClass;
        this.filters = filters;
        this.handlers = handlers;
        this.objectMapper = objectMapper;
    }

    public void handleMessage(Message<?> message) {
        Object payloadClassName = message.getHeaders().get(EventHeader.EVENT_TYPE.value());
        if (payloadClassName == null) {
            log.warn("Invalid message: {} header not present on message {}", EventHeader.EVENT_TYPE.value(), message);
        } else if (!this.eventClass.getName().equals(payloadClassName)) {
            log.warn("Not handling message of type {} (expected type {})", payloadClassName, this.eventClass.getName());
        } else {
            T event = this.extractEvent(message);
            if (event != null) {
                EnvelopedEvent<T> envelopedEvent = new EnvelopedEvent<>();
                envelopedEvent.setEvent(event);
                EventFilterChain<T> filterChain = new EventFilterChainImpl(this.filters, this.handlers);

                try {
                    filterChain.doFilter(message, envelopedEvent);
                } catch (RuntimeException var7) {
                    log.error("Error handling event: [{}]", event, var7);
                    throw var7;
                }
            }
        }

    }

    private T extractEvent(Message message) {
        MimeType contentType = this.getMimeType(message);
        if (contentType != null && contentType.isCompatibleWith(MimeTypeUtils.APPLICATION_JSON)) {
            Object payload = message.getPayload();
            if (payload instanceof byte[]) {
                Charset charset = contentType.getCharset();
                if (charset == null) {
                    charset = StandardCharsets.UTF_8;
                }

                payload = new String((byte[])payload, charset);
            }

            log.trace("Event received: {}", payload);
            T event = null;
            if (payload instanceof String) {
                try {
                    event = this.objectMapper.readValue((String)payload, this.eventClass);
                } catch (IOException var6) {
                    log.warn("Error unmarshalling event of type {}. Event handler and event source may have incompatible implementations of the given Event type.", this.eventClass, var6);
                }
            } else if (this.eventClass.isAssignableFrom(payload.getClass())) {
                event = this.eventClass.cast(payload);
            } else {
                log.warn("Unsupported message payload type: {}. Events should be transmitted as JSON.", payload.getClass().getName());
            }

            return event;
        } else {
            log.warn("Unsupported message content-type: {}. Events should be transmitted as JSON.", message.getHeaders().get("contentType"));
            return null;
        }
    }

    private MimeType getMimeType(Message message) {
        Object contentType = message.getHeaders().get("contentType");
        return contentType instanceof MimeType ? (MimeType)contentType : null;
    }
}
