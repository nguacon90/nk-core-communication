package com.nk.communication.event;

public enum EventHeader {
    EVENT_TYPE("nkEventType"),
    SECURITY_CONTEXT("nkSecurityContext"),
    LOCATION("nkLocation"),
    LOCATION_TYPE("nkLocationType"),
    CREATION_TIME("nkCreationTime"),
    USER_AGENT("nkUserAgent"),
    REQUEST_UUID("nkRequestUUID"),
    ;
    private final String value;
    EventHeader(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
