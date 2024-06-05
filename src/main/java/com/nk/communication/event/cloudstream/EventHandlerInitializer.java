package com.nk.communication.event.cloudstream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nk.communication.event.Event;
import com.nk.communication.event.handler.EventHandler;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binding.Bindable;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.messaging.DirectWithAttributesChannel;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.messaging.SubscribableChannel;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Data
public class EventHandlerInitializer implements Bindable {
    private final GenericApplicationContext applicationContext;
    private final List<EventMessageFilter> eventMessageFilters;
    private final SubscribableChannelInputFactory channelFactory;
    private final ObjectMapper objectMapper;
    private final Map<Class<? extends Event>, List<EventHandler>> handlersByEventClass = new HashMap<>();
    private final Map<String, SubscribableChannel> inputs = new HashMap<>();
    private List<Binding<Object>> bindings = new ArrayList<>();

    private Collection<? extends Binding<Object>> initHandlers(Class<? extends Event> eventClass, List<EventHandler> list, BindingService bindingService) {
        String inputName = this.getInputName(eventClass);
        DirectWithAttributesChannel input = this.channelFactory.createInput(inputName);
        this.inputs.put(inputName, input);
        input.subscribe(new EventMessageHandler(eventClass, this.eventMessageFilters, list, this.objectMapper));
        return bindingService.bindConsumer(input, inputName);
    }

    private Class<? extends Event> getEventType(EventHandler handler) {
        Class<?> eventHandlerClass = AopProxyUtils.ultimateTargetClass(handler);
        Class[] typeArguments = GenericTypeResolver.resolveTypeArguments(eventHandlerClass, EventHandler.class);
        return typeArguments != null && Event.class.isAssignableFrom(typeArguments[0]) ? typeArguments[0] : null;
    }

    public Collection<Binding<Object>> createAndBindInputs(BindingService bindingService) {
        if (!this.bindings.isEmpty()) {
            return this.bindings;
        } else {
            Map<String, EventHandler> eventHandlers = this.applicationContext.getBeansOfType(EventHandler.class);
            Iterator var3 = eventHandlers.values().iterator();

            while(var3.hasNext()) {
                EventHandler handler = (EventHandler)var3.next();
                Class<? extends Event> eventType = this.getEventType(handler);
                if (eventType == null) {
                    log.warn("Could not determine Event type for EventHandler {}; event handler will not receive any events.", AopProxyUtils.ultimateTargetClass(handler));
                } else {
                    this.handlersByEventClass.computeIfAbsent(eventType, (key) -> new ArrayList<>()).add(handler);
                }
            }

            var3 = this.handlersByEventClass.entrySet().iterator();

            while(var3.hasNext()) {
                Map.Entry<Class<? extends Event>, List<EventHandler>> entry = (Map.Entry)var3.next();
                this.bindings.addAll(this.initHandlers(entry.getKey(), entry.getValue(), bindingService));
            }

            return this.bindings;
        }
    }

    public void unbindInputs(BindingService bindingService) {
        Iterator<String> var2 = this.inputs.keySet().iterator();

        while(var2.hasNext()) {
            String inputName = var2.next();
            bindingService.unbindConsumers(inputName);
        }

        this.inputs.clear();
        this.handlersByEventClass.clear();
    }

    private String getInputName(Class<? extends Event> eventClass) {
        return eventClass.getName();
    }

    public Set<String> getInputs() {
        return this.inputs.keySet();
    }
}
