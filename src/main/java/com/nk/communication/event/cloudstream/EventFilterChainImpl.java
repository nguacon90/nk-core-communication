package com.nk.communication.event.cloudstream;

import com.nk.communication.event.EnvelopedEvent;
import com.nk.communication.event.Event;
import com.nk.communication.event.handler.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import java.util.Iterator;
import java.util.List;

public class EventFilterChainImpl <T extends Event> implements EventFilterChain<T> {
    private static final Logger log = LoggerFactory.getLogger(EventFilterChainImpl.class);
    private final Iterator<EventMessageFilter> filters;
    private final List<EventHandler<T>> eventHandlers;

    public EventFilterChainImpl(List<EventMessageFilter> filters, List<EventHandler<T>> eventHandlers) {
        this.filters = filters.iterator();
        this.eventHandlers = eventHandlers;
    }

    @Override
    public void doFilter(Message message, EnvelopedEvent<T> envelopedEvent) {
        if (this.filters.hasNext()) {
            try {
                ((EventMessageFilter)this.filters.next()).filterReceivedEvent(message, envelopedEvent, this);
            } catch (MessagingException var7) {
                log.debug("MessagingException in EventMessageFilter; rethrowing");
                throw var7;
            } catch (Exception var8) {
                log.debug("Unexpected exception in EventMessageFilter; throwing MessagingException");
                throw new MessagingException("An EventMessageFilter threw an Exception", var8);
            }
        } else {
            Iterator<EventHandler<T>> var3 = this.eventHandlers.iterator();

            while(var3.hasNext()) {
                EventHandler<T> handler = var3.next();

                try {
                    handler.handle(envelopedEvent);
                } catch (Exception var6) {
                    log.debug("Unexpected exception in EventMessageHandler; throwing MessagingException");
                    throw new MessagingException("An EventHandler threw an Exception", var6);
                }
            }
        }

    }
}