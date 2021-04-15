package com.useriq.sdk.models;

import java.util.Map;

/**
 * @author sudhakar
 * @created 09-Oct-2018
 */
public class STStep {
    public final String id;
    public final Element element;
    public final String title;
    public final String desc;

    STStep(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.element = new Element((Map<String, Object>) map.get("element"));
        this.title = (String) map.get("title");
        this.desc = (String) map.get("desc");
    }
}
