package com.nk.communication.event.config;

import com.nk.communication.event.exceptions.AzureSubscriberNameCheckException;
import com.nk.communication.event.exceptions.PropertiesSuggestion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
@ConditionalOnProperty(
        value = {"nk.events.subscriber-length-check"},
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
public class AzureSubscriberNameCheck {
    public static final String CHECKING_SUBSCRIBER_NAMES = "Checking Topic Subscriber Names do not exceed the 50 character limit on Azure, to skip the set nk.events.subscriber-length-check=false";
    public static final String EXCEPTION_MESSAGE = "Invalid events consumer groups values";
    private static final int AZURE_TOPIC_SUBSCRIBER_NAME_SIZE_LIMIT = 50;
    private final ConfigurableEnvironment environment;

    public AzureSubscriberNameCheck(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @EventListener({ApplicationReadyEvent.class})
    public void check() {
        log.info(CHECKING_SUBSCRIBER_NAMES);
        String springAppName = this.environment.getProperty("spring.application.name");
        boolean failStartup = false;
        MutablePropertySources sources = this.environment.getPropertySources();
        List<PropertiesSuggestion> propertiesSuggestions = new ArrayList();
        if (StringUtils.isNotEmpty(springAppName)) {
            try {
                Stream<PropertySource<?>> var10000 = StreamSupport.stream(sources.spliterator(), false);
                Objects.requireNonNull(EnumerablePropertySource.class);
                List<String> result = var10000.filter(EnumerablePropertySource.class::isInstance)
                        .map((ps) -> ((EnumerablePropertySource<?>)ps).getPropertyNames())
                        .flatMap(Arrays::stream).distinct()
                        .filter((prop) -> prop.startsWith("spring.cloud.stream") && prop.endsWith("group"))
                        .filter((prop) -> Objects.requireNonNull(this.environment.getProperty(prop)).length() > AZURE_TOPIC_SUBSCRIBER_NAME_SIZE_LIMIT)
                        .toList();
                if (!result.isEmpty() && log.isErrorEnabled()) {
                    failStartup = true;
                    result.forEach((prop) -> {
                        String newValue = this.suggestNewValue(Objects.requireNonNull(this.environment.getProperty(prop)), springAppName);
                        if (newValue.length() <= 50) {
                            propertiesSuggestions.add(new PropertiesSuggestion(prop, this.environment.getProperty(prop), newValue));
                        } else {
                            propertiesSuggestions.add(new PropertiesSuggestion(prop, this.environment.getProperty(prop), null));
                        }

                    });
                }
            } catch (Exception var6) {
                failStartup = false;
                log.info("Unable to validate topic subscriber names");
            }

            if (failStartup) {
                throw new AzureSubscriberNameCheckException(EXCEPTION_MESSAGE, propertiesSuggestions);
            }
        }

    }

    private String suggestNewValue(String value, String springAppName) {
        return value.contains(springAppName) ? springAppName + "." + this.getLastWord(value) : this.getLastWord(value);
    }

    private String getLastWord(String value) {
        return value.contains(".") ? value.substring(value.lastIndexOf(".") + 1) : value;
    }
}
