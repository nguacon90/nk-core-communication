package com.nk.communication.event;

import com.nk.communication.context.OriginatorContext;
import lombok.Data;

@Data
public class EnvelopedEvent<T> {
    private T event;
    private OriginatorContext originatorContext;
}
