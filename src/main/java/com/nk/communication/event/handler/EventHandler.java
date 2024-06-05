package com.nk.communication.event.handler;

import com.nk.communication.event.EnvelopedEvent;

@FunctionalInterface
public interface EventHandler<T> {
    void handle(EnvelopedEvent<T> event);
}
