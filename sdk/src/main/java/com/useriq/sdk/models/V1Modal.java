package com.useriq.sdk.models;

import java.util.Map;

public class V1Modal {
    public final static String MODAL_TYPE_inAppDefault = "inAppDefault";
    public final static String MODAL_TYPE_inAppHeader = "inAppHeader";
    public final static String MODAL_TYPE_inAppFooter = "inAppFooter";
    public final static String MODAL_TYPE_inAppCover = "inAppCover";
    public final static String MODAL_TYPE_starDefault = "starDefault";
    public final static String MODAL_TYPE_starHeader = "starHeader";
    public final static String MODAL_TYPE_starFooter = "starFooter";
    public final static String MODAL_TYPE_starCover = "starCover";
    public final static String MODAL_TYPE_npsDefault = "npsDefault";
    public final static String MODAL_TYPE_npsHeader = "npsHeader";
    public final static String MODAL_TYPE_npsFooter = "npsFooter";
    public final static String MODAL_TYPE_npsCover = "npsCover";

    public final String id;
    public final String name;
    public final String type;
    public final Node layout;

    public V1Modal(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.type = (String) map.get("type");
        this.layout = (Node) UINode.getUiNode((Map) map.get("layout"));
    }

}
