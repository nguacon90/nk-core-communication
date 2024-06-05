package com.nk.communication.event.cloudstream;

import com.nk.communication.context.OriginatorContext;
import org.springframework.messaging.support.MessageBuilder;

@FunctionalInterface
public interface MessageFactoryProcessor {
    <T> void prepareEventMessage(MessageBuilder<T> messageBuilder, OriginatorContext context);
}
