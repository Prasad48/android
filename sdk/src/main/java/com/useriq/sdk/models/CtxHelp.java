package com.useriq.sdk.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author sudhakar
 * @created 05-Oct-2018
 */
public class CtxHelp {
    public final String id;
    public final String screenId;
    public final List<CtxHelpItem> entries;

    public CtxHelp(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.screenId = (String) map.get("screenId");
        this.entries = new ArrayList<>();

        List<Map<String, String>> entries = (List<Map<String, String>>) map.get("entries");

        for(Map<String, String> item: entries) {
            this.entries.add(new CtxHelpItem(item));
        }
    }
}
