package com.nk.communication.event.config;


import brave.Tracing;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nk.communication.event.cloudstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.cloud.sleuth.autoconfig.TraceConfiguration;
import org.springframework.cloud.sleuth.autoconfig.brave.BraveAutoConfiguration;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binding.CompositeMessageChannelConfigurer;
import org.springframework.cloud.stream.config.BindingServiceConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.Order;

import java.util.List;

@ConditionalOnProperty(
        name = {"nk.events.enabled"},
        havingValue = "true",
        matchIfMissing = true
)
@Configuration
@ConditionalOnClass({Binder.class})
@AutoConfigureAfter({BraveAutoConfiguration.class, TraceConfiguration.class, JacksonAutoConfiguration.class})
@AutoConfigureBefore({BindingServiceConfiguration.class})
public class EventBindableAutoConfiguration {
    public EventBindableAutoConfiguration() {
    }

    @Bean
    public EventHandlerInitializer eventHandlerInitialiser(List<EventMessageFilter> eventMessageFilters, SubscribableChannelInputFactory channelFactory,
                                                           ObjectMapper objectMapper, GenericApplicationContext applicationContext) {
        return new EventHandlerInitializer(applicationContext, eventMessageFilters, channelFactory, objectMapper);
    }

    @Bean
    public SubscribableChannelInputFactory subscribableChannelInputFactory(CompositeMessageChannelConfigurer messageChannelConfigurer) {
        return new SubscribableChannelInputFactory(messageChannelConfigurer);
    }

    @Bean
    @ConditionalOnBean({Tracing.class})
    @ConditionalOnProperty(
            prefix = "nk.events",
            name = {"tracing-event-message-filter"},
            havingValue = "true",
            matchIfMissing = true
    )
    @Order(5)
    public TracingMessageInProcessor tracingEventInMessageProcessor(@Value("${spring.application.name}") String appName, Tracing tracing) {
        return new TracingMessageInProcessor(appName, tracing);
    }

    @Bean
    @Order(10)
    public OriginatorContextEventMessageProcessor originatorContextEventMessageProcessor() {
        return new OriginatorContextEventMessageProcessor();
    }
}