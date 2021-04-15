package com.useriq.sdk.models;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Button extends UINode {
    public final static int CLOSE = 0;
    public final static int OPEN_URL = 1;
    public static final int TRACK = 2;
    public static final int SUBMIT = 3;

    private String label;
    private int action;
    private String url;
    private List<Integer> color;
    private float textSize;

    Button(Map map) {
        type = BUTTON;
        String action = (String) map.get("action");
        switch (action) {
            case "CLOSE":
                this.action = Button.CLOSE;
                break;
            case "OPEN_URL":
                this.action = Button.OPEN_URL;
                break;
            case "TRACK":
                this.action = Button.TRACK;
                break;
            case "SUBMIT":
                this.action = Button.SUBMIT;
        }
        this.label = (String) map.get("label");
        if (map.containsKey("color")) {
            List<Long> color = (List<Long>) map.get("color");
            if (color != null) {
                this.color = new ArrayList<>();
                for (int i = 0; i < color.size(); i++) {
                    this.color.add(color.get(i).intValue());
                }
            }
        }
        if (map.containsKey("textSize")) {
            this.textSize = ((Long) map.get("textSize")).intValue();
        }
        if (map.containsKey("url")) {
            this.url = (String) map.get("url");
        }
    }

    public String getLabel() {
        return label;
    }

    public int getAction() {
        return action;
    }

    public String getUrl() {
        return url;
    }

    public List<Integer> getColor() {
        return color;
    }

    public float getTextSize() {
        return textSize;
    }
}