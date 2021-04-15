package com.useriq.sdk.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Text extends UINode {
    public final static int CLOSE = 0;
    public final static int OPEN_URL = 1;
    public static final int TRACK = 2;
    public static final int SUBMIT = 3;
    public static final int WT = 4;
    public static final int QUESTION = 5;
    public static final int WEBVIEW = 6;
    public static final int OPEN_MODAL = 7;


    private String text;
    private int textSize;
    private int action;
    private String url;
    private String value;
    private String alignX, alignY;
    private List<Integer> color;
    private int shadowRadius, shadowDx, shadowDy;

    List<Integer> shadowColor;

    Text(Map map) {
        type = TEXT_VIEW;
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
        if (map.containsKey("text")) {
            this.text = (String) map.get("text");
        } else {
            this.text = (String) map.get("label");
        }

        String action = (String) map.get("action");
        if (action != null) {
            switch (action) {
                case "CLOSE":
                    this.action = Text.CLOSE;
                    break;
                case "OPEN_URL":
                    this.action = Text.OPEN_URL;
                    break;
                case "TRACK":
                    this.action = Text.TRACK;
                    break;
                case "SUBMIT":
                    this.action = Text.SUBMIT;
                    break;
                case "WT":
                    this.action = Text.WT;
                    break;
                case "QUESTION":
                    this.action = Text.QUESTION;
                    break;
                case "OPEN_WEBVIEW":
                    this.action = Text.WEBVIEW;
                    break;
                case "OPEN_MODAL":
                    this.action = Text.OPEN_MODAL;
                    break;
            }
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
        if (map.containsKey("textSize")) {
            this.textSize = ((Long) map.get("textSize")).intValue();
        }
        if (map.containsKey("url")) {
            this.url = (String) map.get("url");
        }
        if (map.containsKey("value")) {
            this.value = (String) map.get("value");
        }
    }

    public String getText() {
        return text;
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

    public int getAction() {
        return action;
    }

    public String getUrl() {
        return url;
    }

    public String getValue() {
        return value;
    }
}