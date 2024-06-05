package com.nk.communication.event.cloudstream;

import com.nk.communication.event.EnvelopedEvent;
import com.nk.communication.event.Event;
import org.springframework.messaging.support.MessageBuilder;

@FunctionalInterface
public interface EventMessageProcessor {
    <T extends Event> void prepareEventMessage(MessageBuilder<T> var1, EnvelopedEvent<T> var2);
}
