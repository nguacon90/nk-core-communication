package com.nk.communication.event.cloudstream;

import brave.propagation.Propagation;
import org.springframework.messaging.Message;

import java.nio.charset.StandardCharsets;

public class MessageStringGetter implements Propagation.Getter<Message, String> {
    @Override
    public String get(Message message, String key) {
        Object value = message.getHeaders().get(key);
        if (value == null) {
            return null;
        } else if (value instanceof byte[]) {
            return new String((byte[])value, StandardCharsets.UTF_8);
        } else {
            return value instanceof String ? (String)value : String.valueOf(value);
        }
    }
}
