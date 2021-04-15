package com.useriq.sdk.models;

import java.util.List;
import java.util.Map;

public class Node extends UINode {
    public UINode[] children;

    private Node() {
        type = NODE;
    }

    public static Node getNode(Map map) {
        List<Map> maps = (List<Map>) map.get("children");
        Node node = new Node();
        node.children = new UINode[maps.size()];
        for (int i = 0; i < maps.size(); i++) {
            node.children[i] = getUiNode(maps.get(i));
        }
        return node;
    }
}