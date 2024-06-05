package com.nk.communication.event.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "nk.events")
@Data
public class EventsConfiguration {
    private boolean enabled = true;
    private Set<String> disabledEvents;
}
