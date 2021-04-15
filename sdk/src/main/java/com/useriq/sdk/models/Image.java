package com.useriq.sdk.models;

import java.util.Map;

public class Image extends UINode {
    public boolean crop;
    public String assetId;
    public String alignX, alignY;

    Image(Map map) {
        type = IMAGE_VIEW;
        this.crop = (boolean) map.get("crop");
        this.alignX = (String) map.get("alignX");
        this.alignY = (String) map.get("alignY");
        this.assetId = (String) map.get("assetId");
    }
}