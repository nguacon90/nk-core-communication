package com.nk.communication.event.cloudstream;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;

@Slf4j
public class TracingMessageInProcessor implements MessageInProcessor {
    private final String appName;
    private final Tracer tracer;
    private final TraceContext.Extractor<Message> spanExtractor;
    public TracingMessageInProcessor(String appName, Tracing tracing) {
        this.appName = appName;
        this.tracer = tracing.tracer();
        this.spanExtractor = tracing.propagation().extractor(new MessageStringGetter());
    }
    @Override
    public <T> void processPreReceived(Message<T> message, String channelName, String messageType) {
        TraceContextOrSamplingFlags extracted = this.spanExtractor.extract(message);
        Span span;
        if (extracted.context() == null) {
            log.debug("Next Span because context is null");
            span = this.tracer.nextSpan(extracted);
        } else {
            log.debug("Creating a child within an existing trace");
            span = this.tracer.newChild(extracted.context());
        }

        span.name(this.appName).kind(Span.Kind.CONSUMER).tag("event.type", messageType).tag("event.channelName", channelName).start();
        this.tracer.withSpanInScope(span);
        span.flush();
        log.debug("Joined span: {}", span);
    }

    @Override
    public <T> void processPostReceived(Message<T> message, String channelName, String messageType) {
        if (this.tracer.currentSpan() != null) {
            this.tracer.currentSpan().finish();
        }
    }
}
