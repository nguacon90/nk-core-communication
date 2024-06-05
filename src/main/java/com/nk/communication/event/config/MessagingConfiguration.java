package com.nk.communication.event.config;

import com.nk.communication.event.cloudstream.EventHandlerInitializer;
import com.nk.communication.event.cloudstream.MessageProcessorChannelInterceptor;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.config.BindingServiceConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.GlobalChannelInterceptor;

@Configuration
@ConditionalOnProperty(
        name = {"nk.events.enabled"},
        havingValue = "true",
        matchIfMissing = true
)
@ConditionalOnClass({Binder.class})
@AutoConfigureAfter({BindingServiceConfiguration.class})
public class MessagingConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MessagingConfiguration.class);
    @Autowired
    private BindingService bindingService;
    @Autowired
    private EventHandlerInitializer eventHandlerInitialiser;

    @PostConstruct
    void initBinder() {
        log.debug("Creating binding inputs on MessagingConfiguration");
        this.eventHandlerInitialiser.createAndBindInputs(this.bindingService);
    }

    @Bean
    @GlobalChannelInterceptor
    public MessageProcessorChannelInterceptor interceptor() {
        return new MessageProcessorChannelInterceptor();
    }
}

