package com.nk.communication.event.exceptions;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PropertiesSuggestion implements Serializable {
    @Serial
    private static final long serialVersionUID = 6234669243349209731L;
    private String key;
    private String currentValue;
    private String suggestedValue;

    public PropertiesSuggestion(String key, String currentValue, String suggestedValue) {
        this.key = key;
        this.currentValue = currentValue;
        this.suggestedValue = suggestedValue;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            PropertiesSuggestion that;
            label41: {
                that = (PropertiesSuggestion)o;
                if (this.key != null) {
                    if (this.key.equals(that.key)) {
                        break label41;
                    }
                } else if (that.key == null) {
                    break label41;
                }

                return false;
            }

            if (this.currentValue != null) {
                if (this.currentValue.equals(that.currentValue)) {
                    return this.suggestedValue != null ? this.suggestedValue.equals(that.suggestedValue) : that.suggestedValue == null;
                }
            } else if (that.currentValue == null) {
                return this.suggestedValue != null ? this.suggestedValue.equals(that.suggestedValue) : that.suggestedValue == null;
            }

            return false;
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.key != null ? this.key.hashCode() : 0;
        result = 31 * result + (this.currentValue != null ? this.currentValue.hashCode() : 0);
        result = 31 * result + (this.suggestedValue != null ? this.suggestedValue.hashCode() : 0);
        return result;
    }
}
