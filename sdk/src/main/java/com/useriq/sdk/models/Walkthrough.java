package com.useriq.sdk.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author sudhakar
 * @created 05-Oct-2018
 */
public class Walkthrough {
    public final String id;
    public final String name;
    public final List<WTStep> steps;

    public Walkthrough(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.steps = new ArrayList<>();

        List<Map<String, Object>> wtSteps = (List<Map<String, Object>>) map.get("steps");

        if(wtSteps == null) return;

        for(Map<String, Object> step: wtSteps) {
            this.steps.add(new WTStep(step));
        }
    }
}
