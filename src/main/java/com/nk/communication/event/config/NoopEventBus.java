package com.nk.communication.event.config;

import com.nk.communication.event.EnvelopedEvent;
import com.nk.communication.event.Event;
import com.nk.communication.event.proxy.EventBus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoopEventBus implements EventBus {
    private boolean shouldLogMessage = true;

    public void emitEvent(EnvelopedEvent<? extends Event> event) {
        if (this.shouldLogMessage) {
            this.shouldLogMessage = false;
            log.info("Eventing has been disabled but an event of type {} was emitted.  " +
                     "All events will be silently discarded.  This message will not be logged again if any further events are emitted.", event.getEvent().getClass());
        }
    }
}
