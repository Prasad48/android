package com.useriq.sdk.models;

import java.util.Map;

/**
 * @author sudhakar
 * @created 05-Oct-2018
 */
public class CtxHelpItem {
    public final Type type;
    public final String id;

    public CtxHelpItem(Map<String, String> map) {
        this.type = Type.valueOf(map.get("type"));
        this.id = map.get("id");
    }

    public enum Type {
        walkthrough,
        question,
    }
}
