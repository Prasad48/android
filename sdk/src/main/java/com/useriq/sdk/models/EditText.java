package com.useriq.sdk.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EditText extends UINode {
    public final static int CLOSE = 0;
    public final static int OPEN_URL = 1;
    public static final int TRACK = 2;
    public static final int SUBMIT = 3;
    public static final int WT = 4;
    public static final int QUESTION = 5;

    private int textSize;
    private String placeholder;
    private String alignX, alignY;
    private List<Integer> color;
    private List<Integer> placeholderColor;
    private int shadowRadius, shadowDx, shadowDy;

    List<Integer> shadowColor;

    EditText(Map map) {
        type = EDIT_TEXT;
        if (map.containsKey("alignX")) {
            this.alignX = (String) map.get("alignX");
        } else {
            this.alignX = UINode.CENTER;
        }
        if (map.containsKey("alignY")) {
            this.alignY = (String) map.get("alignY");
        } else {
            this.alignY = UINode.CENTER;
        }

        if (map.containsKey("textShadow")) {
            List<Object> textShadow = (List<Object>) map.get("textShadow");
            this.shadowRadius = ((Long) textShadow.get(2)).intValue();
            this.shadowDx = ((Long) textShadow.get(0)).intValue();
            this.shadowDy = ((Long) textShadow.get(1)).intValue();
            List<Long> shadowColor = (List<Long>) textShadow.get(3);
            this.shadowColor = new ArrayList<>();
            for (int i = 0; i < shadowColor.size(); i++) {
                this.shadowColor.add(shadowColor.get(i).intValue());
            }
        }
        if (map.containsKey("color")) {
            List<Long> color = (List<Long>) map.get("color");
            if (color != null) {
                this.color = new ArrayList<>();
                for (int i = 0; i < color.size(); i++) {
                    this.color.add(color.get(i).intValue());
                }
            }
        }
        if (map.containsKey("placeholderColor")) {
            List<Long> placeholderColor = (List<Long>) map.get("placeholderColor");
            if (placeholderColor != null) {
                this.placeholderColor = new ArrayList<>();
                for (int i = 0; i < placeholderColor.size(); i++) {
                    this.placeholderColor.add(placeholderColor.get(i).intValue());
                }
            }
        }

        if (map.containsKey("textSize")) {
            this.textSize = ((Long) map.get("textSize")).intValue();
        } else {
            this.textSize = 12;
        }
        if (map.containsKey("placeholder")) {
            this.placeholder = (String) map.get("placeholder");
        }
    }

    public int getTextSize() {
        return textSize;
    }

    public String getAlignX() {
        return alignX;
    }

    public String getAlignY() {
        return alignY;
    }

    public List<Integer> getColor() {
        return color;
    }

    public int getShadowRadius() {
        return shadowRadius;
    }

    public int getShadowDx() {
        return shadowDx;
    }

    public int getShadowDy() {
        return shadowDy;
    }

    public List<Integer> getShadowColor() {
        return shadowColor;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public List<Integer> getPlaceholderColor() {
        return placeholderColor;
    }
}