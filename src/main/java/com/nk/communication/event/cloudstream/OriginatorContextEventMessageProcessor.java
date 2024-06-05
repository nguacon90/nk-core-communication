package com.nk.communication.event.cloudstream;

import com.nk.communication.context.OriginatorContext;
import com.nk.communication.event.EnvelopedEvent;
import com.nk.communication.event.Event;
import com.nk.communication.event.EventHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

@Slf4j
public class OriginatorContextEventMessageProcessor implements EventMessageProcessor, EventMessageFilter, MessageFactoryProcessor {
    @Override
    public <T extends Event> void prepareEventMessage(MessageBuilder<T> eventMessageBuilder, EnvelopedEvent<T> envelopedEvent) {
        OriginatorContext context = envelopedEvent.getOriginatorContext();
        this.prepareEventMessage(eventMessageBuilder, context);
    }

    public <T> void prepareEventMessage(MessageBuilder<T> messageBuilder, OriginatorContext context) {
        if (context != null) {
            this.setIfNotNull(messageBuilder, EventHeader.SECURITY_CONTEXT.value(), context.getSecurityContext());
            this.setIfNotNull(messageBuilder, EventHeader.LOCATION.value(), context.getLocation());
            this.setIfNotNull(messageBuilder, EventHeader.LOCATION_TYPE.value(), context.getLocationType());
            this.setIfNotNull(messageBuilder, EventHeader.CREATION_TIME.value(), context.getCreationTime());
            this.setIfNotNull(messageBuilder, EventHeader.USER_AGENT.value(), context.getUserAgent());
            this.setIfNotNull(messageBuilder, EventHeader.REQUEST_UUID.value(), context.getRequestUuid());
        }

    }

    private void setIfNotNull(MessageBuilder<?> eventMessageBuilder, String header, Object value) {
        if (value instanceof Enum) {
            eventMessageBuilder.setHeader(header, ((Enum)value).name());
        } else if (value != null) {
            eventMessageBuilder.setHeader(header, value.toString());
        }

    }

    public <T extends Event> void filterReceivedEvent(Message message, EnvelopedEvent<T> envelopedEvent, EventFilterChain<T> filterChain) {
        OriginatorContext context = new OriginatorContext();
        context.setSecurityContext(this.getIfString(message, EventHeader.SECURITY_CONTEXT.value()));
        context.setLocation(this.getIfString(message, EventHeader.LOCATION.value()));
        String locationTypeString = this.getIfString(message, EventHeader.LOCATION_TYPE.value());
        if (locationTypeString != null) {
            try {
                context.setLocationType(OriginatorContext.LocationType.valueOf(locationTypeString));
            } catch (IllegalArgumentException var12) {
                this.logIllegalHeaderValue(var12, EventHeader.LOCATION_TYPE.value(), locationTypeString);
            }
        }

        String creationTimeStr = this.getIfString(message, EventHeader.CREATION_TIME.value());
        if (creationTimeStr != null) {
            try {
                context.setCreationTime(Long.valueOf(creationTimeStr));
            } catch (NumberFormatException var11) {
                this.logIllegalHeaderValue(var11, EventHeader.CREATION_TIME.value(), creationTimeStr);
            }
        }

        context.setUserAgent(this.getIfString(message, EventHeader.USER_AGENT.value()));
        context.setRequestUuid(this.getIfString(message, EventHeader.REQUEST_UUID.value()));
        envelopedEvent.setOriginatorContext(context);
        filterChain.doFilter(message, envelopedEvent);
    }

    private void logIllegalHeaderValue(IllegalArgumentException ex, String header, String value) {
        log.warn("Illegal value ({}) of {} header: {}. Event message header values should match the expected type", new Object[]{ex.getClass().getSimpleName(), header, value});
    }

    private String getIfString(Message message, String header) {
        Object value = message.getHeaders().get(header);
        return value instanceof String ? (String)value : null;
    }
}
