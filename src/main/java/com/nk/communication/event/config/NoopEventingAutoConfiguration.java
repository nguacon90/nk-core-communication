package com.nk.communication.event.config;


import com.nk.communication.event.handler.EventHandler;
import com.nk.communication.event.proxy.EventBus;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnProperty(
        name = {"nk.events.enabled"},
        havingValue = "false"
)
@EnableConfigurationProperties({EventsConfiguration.class})
@Slf4j
@Data
class NoopEventingAutoConfiguration implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Bean
    public EventBus eventBus() {
        return new NoopEventBus();
    }

    @PostConstruct
    public void init() {
        Map<String, EventHandler> eventHandlers = this.applicationContext.getBeansOfType(EventHandler.class);
        if (!eventHandlers.isEmpty()) {
            log.info("NK Eventing is disabled.  The following EventHandlers will never receive any Events: {}", eventHandlers.values().stream().map(Object::getClass).map(Class::getName).collect(Collectors.toList()));
        }

    }
}
