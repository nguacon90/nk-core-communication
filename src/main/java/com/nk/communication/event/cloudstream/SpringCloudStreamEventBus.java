package com.nk.communication.event.cloudstream;

import com.nk.communication.event.EnvelopedEvent;
import com.nk.communication.event.Event;
import com.nk.communication.event.EventHeader;
import com.nk.communication.event.config.EventsConfiguration;
import com.nk.communication.event.proxy.EventBus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Data
public class SpringCloudStreamEventBus implements EventBus {

    @Autowired
    private StreamBridge bridge;

    @Autowired
    private EventsConfiguration eventsConfiguration;

    @Autowired(required = false)
    private List<EventMessageProcessor> eventMessageProcessors = Collections.emptyList();
    @Override
    public void emitEvent(EnvelopedEvent<? extends Event> envelopedEvent) {
        Objects.requireNonNull(envelopedEvent, "envelopedEvent must not be null");
        Object wrappedEvent = envelopedEvent.getEvent();
        if (wrappedEvent == null) {
            throw new IllegalArgumentException("EnvelopedEvent must wrap an Event object - null was passed");
        }

        this.emitEventInternal(envelopedEvent);
    }

    private <T extends Event> void emitEventInternal(EnvelopedEvent<T> envelopedEvent) {
        T event = envelopedEvent.getEvent();
        MessageBuilder<T> eventMessageBuilder = MessageBuilder.withPayload(event)
                .setHeader(EventHeader.EVENT_TYPE.value(), event.getClass().getName());
        this.eventMessageProcessors.forEach((processor) -> processor.prepareEventMessage(eventMessageBuilder, envelopedEvent));
        Message<T> message = eventMessageBuilder.build();
        log.trace("Event emitted: {}", message.getPayload());
        String channelName = event.getClass().getName();
        if (this.eventsConfiguration.getDisabledEvents() != null && this.eventsConfiguration.getDisabledEvents().contains(channelName)) {
            log.debug("Event message {} is not sent as {} is disabled", event, channelName);
        } else {
            if (!this.bridge.send(channelName, message)) {
                log.debug("Failed to send event message for event {} on channel {}. Check event message broker is available.", event, channelName);
                log.error("Failed to send event message on channel {}. Check event message broker is available.", channelName);
            }

        }
    }
}
