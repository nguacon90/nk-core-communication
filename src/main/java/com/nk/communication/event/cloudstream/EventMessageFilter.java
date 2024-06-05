package com.nk.communication.event.cloudstream;

import com.nk.communication.event.EnvelopedEvent;
import com.nk.communication.event.Event;
import org.springframework.messaging.Message;

@FunctionalInterface
public interface EventMessageFilter {
    <T extends Event> void filterReceivedEvent(Message message, EnvelopedEvent<T> event, EventFilterChain<T> eventFilterChain);
}
