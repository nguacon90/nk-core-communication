package com.nk.communication.context;

import lombok.Data;

@Data
public class OriginatorContext {
    private String securityContext;
    private String location;
    private LocationType locationType;
    private Long creationTime;
    private String userAgent;
    private String requestUuid;
    public static enum LocationType {
        HOSTNAME,
        IPV4,
        IPV6,
        UNKNOWN;

        private LocationType() {
        }
    }
}
