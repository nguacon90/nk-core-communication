package com.nk.communication.event.cloudstream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binding.MessageChannelConfigurer;
import org.springframework.cloud.stream.messaging.DirectWithAttributesChannel;
import org.springframework.context.support.GenericApplicationContext;

@Slf4j
@RequiredArgsConstructor
public class SubscribableChannelInputFactory {
    private final MessageChannelConfigurer messageChannelConfigurer;
    @Autowired
    private GenericApplicationContext context;

    public DirectWithAttributesChannel createInput(String channelName) {
        DirectWithAttributesChannel subscribableChannel = new DirectWithAttributesChannel();
        String beanName = channelName + subscribableChannel.hashCode();
        log.debug("Creating SubscribableChannel {}", beanName);
        subscribableChannel.setComponentName(beanName);
        subscribableChannel.setAttribute("type", "input");
        this.messageChannelConfigurer.configureInputChannel(subscribableChannel, channelName);
        if (this.context != null && !this.context.containsBean(beanName)) {
            this.context.registerBean(beanName, DirectWithAttributesChannel.class, () -> subscribableChannel);
        }

        return subscribableChannel;
    }
}
