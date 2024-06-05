package com.nk.communication.event.exceptions;

import java.util.List;

public class AzureSubscriberNameCheckException extends RuntimeException {
    private final List<PropertiesSuggestion> propertiesSuggestions;

    public AzureSubscriberNameCheckException(String errorMessage, List<PropertiesSuggestion> propertiesSuggestions) {
        super(errorMessage);
        this.propertiesSuggestions = propertiesSuggestions;
    }

    public List<PropertiesSuggestion> getPropertiesSuggestions() {
        return this.propertiesSuggestions;
    }
}
