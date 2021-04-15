package com.useriq.sdk.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author sudhakar
 * @created 05-Oct-2018
 */
public class QGroup {
    public final String id;
    public final String name;
    public final List<Question> questions;

    QGroup(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.questions = new ArrayList<>();

        List<Map<String, Object>> qList = (List<Map<String, Object>>) map.get("questions");

        if(qList == null) return;

        for(Map<String, Object> qn: qList) {
            questions.add(new Question(qn));
        }
    }
}
