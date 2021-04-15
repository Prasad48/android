package com.useriq.sdk.models;

import java.util.Map;

/**
 * @author sudhakar
 * @created 05-Oct-2018
 */
public class QDesc {
    public final Type type;
    public final String value;

    public QDesc(Map<String, String> map) {
        this.type = Type.valueOf(map.get("type"));
        this.value = map.get("value");
    }

    public enum Type {
        rtf,
        image,
        wt,
    }
}
