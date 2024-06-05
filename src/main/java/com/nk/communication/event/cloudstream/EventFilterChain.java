package com.nk.communication.event.cloudstream;

import com.nk.communication.event.EnvelopedEvent;
import com.nk.communication.event.Event;
import org.springframework.messaging.Message;

@FunctionalInterface
public interface EventFilterChain<T extends Event> {
    void doFilter(Message message, EnvelopedEvent<T> event);
}
