package com.useriq.sdk.models;

import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

/**
 * @author sudhakar
 * @created 05-Oct-2018
 */
public class WTStep {
    public final String id;
    public final Element element;
    public final String title;
    public final String desc;
    public final WTTheme theme;
    public final int imgRotation;
    public final int imgWidth;
    public final int imgHeight;
    public final String imgId;
    public final int nextStepWait;

    @Nullable
    public final List<String> validScreens;

    public final WTStepFinish finishOn;

    WTStep(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.element = new Element((Map<String, Object>) map.get("element"));
        this.title = (String) map.get("title");
        this.desc = (String) map.get("desc");
        this.theme = new WTTheme((Map<String, Object>) map.get("theme"));

        Map<String, Object> screen = (Map<String, Object>) map.get("screen");
        Map<String, Object> props = (Map<String, Object>) screen.get("props");

        this.imgRotation = (int) (long) props.get("rotation");
        this.imgWidth = (int) (long) props.get("width");
        this.imgHeight = (int) (long) props.get("height");
        this.imgId = (String) screen.get("imageId");
        if (map.containsKey("nextStepWait")) {
            this.nextStepWait = (int) (long) map.get("nextStepWait");
        } else {
            this.nextStepWait = 1000;
        }
        this.validScreens = map.containsKey("validScreens") ? (List<String>) map.get("validScreens") : null;

        List<Map<String, Object>> finishOnList = (List<Map<String, Object>>) map.get("finishOn");

        this.finishOn = new WTStepFinish(finishOnList);
    }

    public boolean isValidOnScreen(Screen screen) {
        return screen == null
                || screen.id == null
                || validScreens == null
                || validScreens.size() == 0
                || validScreens.contains(screen.id);
    }
}
