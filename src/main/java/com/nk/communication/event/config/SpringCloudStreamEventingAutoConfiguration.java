package com.nk.communication.event.config;


import com.nk.communication.event.cloudstream.SpringCloudStreamEventBus;
import com.nk.communication.event.proxy.EventBus;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.autoconfig.TraceConfiguration;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.config.BindingServiceConfiguration;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.cloud.stream.function.FunctionConfiguration;
import org.springframework.cloud.stream.messaging.DirectWithAttributesChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.messaging.converter.CompositeMessageConverter;

@Configuration
@EnableConfigurationProperties({EventsConfiguration.class})
@ConditionalOnProperty(
        name = {"nk.events.enabled"},
        havingValue = "true",
        matchIfMissing = true
)
@ConditionalOnClass({Binder.class})
@AutoConfigureAfter({TraceConfiguration.class, JacksonAutoConfiguration.class, FunctionConfiguration.class, BindingServiceConfiguration.class})
@Slf4j
public class SpringCloudStreamEventingAutoConfiguration {
    @Autowired
    private BindingServiceProperties bindingServiceProperties;
    @Autowired
    private BinderFactory binderFactory;

    @PostConstruct
    void initBinder() {
        String binderConfigurationName = this.bindingServiceProperties.getBinder("default");
        try {
            this.binderFactory.getBinder(binderConfigurationName, DirectWithAttributesChannel.class);
        } catch (NoSuchBeanDefinitionException var3) {
            log.warn("Unable to get binder, set nk.events.enabled=false if events are not required.", var3);
        }

    }

    @Bean
    @ConditionalOnMissingBean
    public EventBus eventBus() {
        return new SpringCloudStreamEventBus();
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "nk.events",
            name = {"serialized-payload-class"},
            havingValue = "string",
            matchIfMissing = true
    )
    public BeanPostProcessor compositeMessageConverterBeanPostProcessor() {
        return new BeanPostProcessor() {
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof CompositeMessageConverter) {
                    ((CompositeMessageConverter)bean).getConverters().forEach((c) -> {
                        if (c instanceof AbstractMessageConverter) {
                            ((AbstractMessageConverter)c).setSerializedPayloadClass(String.class);
                        }

                    });
                }
                return bean;
            }
        };
    }
}