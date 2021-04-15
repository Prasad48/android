package com.useriq.sdk.models;

import com.useriq.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.useriq.sdk.models.Node.getNode;

public abstract class UINode {
    private static final Logger logger = Logger.init(UINode.class.getSimpleName());

    public final static String LEFT = "left";
    public final static String CENTER = "center";
    public final static String BOTTOM = "bottom";
    public final static String RIGHT = "right";
    public final static String TOP = "top";
    public final static String IMAGE_VIEW = "image";
    public final static String TEXT_VIEW = "text";
    public final static String EDIT_TEXT = "editText";
    final static String VIEW = "empty";
    public final static String BUTTON = "button";
    public final static String NODE = "node";
    public final static String RATING = "rating";
    public final static String NPS = "nps";

    public List<Integer> bgColor, borderColor;
    public int borderWidth;
    public List<Integer> borderRadius;
    public String id;
    public String type;
    public String l;
    public String r;
    public String t;
    public String b;
    public int shadowRadius;
    public int shadowDx;
    public int shadowDy;
    public List<Integer> shadowColor;

    public UINode() {
    }

    public static UINode getUiNode(Map map) {
        String type = (String) map.get("type");
        UINode iNode = null;
        switch (type) {
            case IMAGE_VIEW:
                iNode = new Image(map);
                break;
            case BUTTON:
                iNode = new Text(map);
                break;
            case TEXT_VIEW:
                iNode = new Text(map);
                break;
            case EDIT_TEXT:
                iNode = new EditText(map);
                break;
            case NODE:
                iNode = getNode(map);
                break;
            case RATING:
                iNode = new RatingNode(map);
                break;
            case NPS:
                iNode = new NPSNode(map);
        }
        if (iNode == null) {
            logger.e("INode type: " + type + "; not defined", null);
            return null;
        }
        iNode.l = (String) map.get("l");
        iNode.b = (String) map.get("b");
        iNode.r = (String) map.get("r");
        iNode.t = (String) map.get("t");
        iNode.id = (String) map.get("id");
        if (map.containsKey("bgColor")) {
            List<Long> bgColor = (List<Long>) map.get("bgColor");
            if (bgColor != null) {
                iNode.bgColor = new ArrayList<>();
                for (int i = 0; i < bgColor.size(); i++) {
                    iNode.bgColor.add(bgColor.get(i).intValue());
                }
            }
        }
        if (map.containsKey("borderColor")) {
            List<Long> borderColor = (List<Long>) map.get("borderColor");
            if (borderColor != null) {
                iNode.borderColor = new ArrayList<>();
                for (int i = 0; i < borderColor.size(); i++) {
                    iNode.borderColor.add(borderColor.get(i).intValue());
                }
            }
        }
        if (map.containsKey("borderWidth")) {
            iNode.borderWidth = ((Long) map.get("borderWidth")).intValue();
        }
        if (map.containsKey("borderRadius")) {
            List<Long> borderRadius = (List<Long>) map.get("borderRadius");
            if (borderRadius != null) {
                iNode.borderRadius = new ArrayList<>();
                for (int i = 0; i < borderRadius.size(); i++) {
                    iNode.borderRadius.add(borderRadius.get(i).intValue());
                }
            }
        }
        if (map.containsKey("boxShadow")) {
            List<Object> boxShadow = (List<Object>) map.get("boxShadow");
            iNode.shadowRadius = ((Long) boxShadow.get(2)).intValue();
            iNode.shadowDx = ((Long) boxShadow.get(0)).intValue();
            iNode.shadowDy = ((Long) boxShadow.get(1)).intValue();
            List<Long> shadowColor = (List<Long>) boxShadow.get(3);
            iNode.shadowColor = new ArrayList<>();
            for (int i = 0; i < shadowColor.size(); i++) {
                iNode.shadowColor.add(shadowColor.get(i).intValue());
            }
        }
        return iNode;
    }
}
