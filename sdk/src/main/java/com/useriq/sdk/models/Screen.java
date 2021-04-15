package com.useriq.sdk.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sudhakar
 * @created 30-Sep-2018
 */
public class Screen {
    public final String id;
    public final String controller;
    public final List<String> predicates;
    public final List<Element> elements;

    // Derived values
    public final List<Element> predicateEls;
    final Map<String, Element> keyElementMap;

    Screen(Map<String, Object> map) {
        id = (String) map.get("id");
        Map<String, Object> predicateMap = (Map<String, Object>) map.get("predicate");
        controller = (String) predicateMap.get("controller");
        predicates = (List<String>) (predicateMap.get("allOf"));
        List<Map> elList = (List<Map>) map.get("elements");
        elements = new ArrayList<>();

        for (Map<String, Object> elObj: elList) {
            elements.add(new Element(elObj));
        }

        keyElementMap = new HashMap<>();
        for(Element el: elements) {
            keyElementMap.put(el.key, el);
        }

        predicateEls = new ArrayList<>();
        for(String key: predicates) {
            predicateEls.add(keyElementMap.get(key));
        }
    }
}