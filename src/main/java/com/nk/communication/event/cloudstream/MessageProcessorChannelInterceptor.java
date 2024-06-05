package com.nk.communication.event.cloudstream;

import com.nk.communication.event.EventHeader;
import com.nk.communication.event.config.EventsConfiguration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.DirectWithAttributesChannel;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;

import java.util.Collections;
import java.util.List;

@Slf4j
@Data
public class MessageProcessorChannelInterceptor implements ChannelInterceptor {
    @Autowired
    private EventsConfiguration eventsConfiguration;
    @Autowired(required = false)
    private final List<MessageInProcessor> messageInProcessor = Collections.emptyList();

    public Message<?> preSend(Message<?> msg, MessageChannel mc) {
        String type = (String)msg.getHeaders().get(EventHeader.EVENT_TYPE.value());
        if (this.eventsConfiguration.getDisabledEvents() != null && this.eventsConfiguration.getDisabledEvents().contains(type)) {
            return null;
        } else {
            if (mc instanceof DirectWithAttributesChannel mcd) {
                if (this.isAnInputChannel(mcd)) {
                    log.debug("Running PreSend Processor in {}", mcd.getFullChannelName());
                    this.messageInProcessor.forEach((processor) -> {
                        processor.processPreReceived(msg, mcd.getFullChannelName(), (String)msg.getHeaders().get(EventHeader.EVENT_TYPE.value()));
                    });
                }
            }

            log.debug("In preSend");
            return msg;
        }
    }

    public void postSend(Message<?> msg, MessageChannel mc, boolean bln) {
        log.debug("In postSend");
    }

    public void afterSendCompletion(Message<?> msg, MessageChannel mc, boolean bln, @Nullable Exception ex) {
        if (mc instanceof DirectWithAttributesChannel) {
            DirectWithAttributesChannel mcd = (DirectWithAttributesChannel)mc;
            if (this.isAnInputChannel(mcd)) {
                log.debug("Running AfterSendCompletion Processor in {}", mcd.getFullChannelName());
                this.messageInProcessor.forEach((processor) -> {
                    processor.processPostReceived(msg, mcd.getFullChannelName(), (String) msg.getHeaders().get(EventHeader.EVENT_TYPE.value()));
                });
            }
        }

        log.debug("In afterSendCompletion");
    }

    private boolean isAnInputChannel(DirectWithAttributesChannel mcd) {
        return "input".equals(mcd.getAttribute("type"));
    }

    public boolean preReceive(MessageChannel mc) {
        log.debug("In preReceive");
        return true;
    }

    public Message<?> postReceive(Message<?> msg, MessageChannel mc) {
        log.debug("In postReceive");
        return msg;
    }

    public void afterReceiveCompletion(Message<?> msg, MessageChannel mc, Exception excptn) {
        log.debug("In afterReceiveCompletion");
    }
}
