package com.useriq.sdk.models;

import java.util.Map;

/**
 * @author sudhakar
 * @created 30-Sep-2018
 */
public class EventOpts {
    static final String ENABLED = "enabled";
    static final String BUF_SIZE = "bufSize";

    public final boolean enabled;
    public final int buffSize;

    EventOpts() {
        enabled = true;
        buffSize = 0;
    }

    EventOpts(Map<String, Object> map) {
        enabled = (boolean) map.get(ENABLED);
        buffSize = ((Long) map.get(BUF_SIZE)).intValue();
    }
}
