package com.nk.communication.event.cloudstream;

import org.springframework.messaging.Message;

public interface MessageInProcessor {
    <T> void processPreReceived(Message<T> message, String channelName, String messageType);

    <T> void processPostReceived(Message<T> message, String channelName, String messageType);
}
