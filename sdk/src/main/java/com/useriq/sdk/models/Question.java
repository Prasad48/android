package com.useriq.sdk.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author sudhakar
 * @created 05-Oct-2018
 */
public class Question {
    public final String id;
    public final String name;
    public final List<QDesc> descList;

    public Question(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.descList = new ArrayList<>();

        List<Map<String, String>> descItems = (List<Map<String, String>>) map.get("desc");

        if (descItems != null) {
            for (Map<String, String> desc : descItems) {
                this.descList.add(new QDesc(desc));
            }
        }
    }
}
