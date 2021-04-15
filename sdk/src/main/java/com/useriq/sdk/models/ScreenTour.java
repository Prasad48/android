package com.useriq.sdk.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author sudhakar
 * @created 09-Oct-2018
 */
public class ScreenTour {
    public final String id;
    public final String name;
    public final String screenId;
    public final boolean isOverlay;
    public final ArrayList<STStep> steps;
    public final STTheme theme;

    public ScreenTour(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.screenId = (String) map.get("screenId");
        this.isOverlay = (boolean) map.get("isOverlay");
        this.steps = new ArrayList<>();

        List<Map<String, Object>> stepList = (List<Map<String, Object>>) map.get("steps");

        for(Map<String, Object> step: stepList) {
            this.steps.add(new STStep(step));
        }

        this.theme = map.containsKey("theme")
                ? new STTheme((Map<String, Object>) map.get("theme")) : null;
    }
}
