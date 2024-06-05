package com.nk.communication.event.proxy;

import com.nk.communication.event.EnvelopedEvent;
import com.nk.communication.event.Event;

public interface EventBus {
    void emitEvent(EnvelopedEvent<? extends Event> event);
}
