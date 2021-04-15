package com.useriq.sdk.models;

import java.util.List;
import java.util.Map;

import static com.useriq.sdk.util.Utils.toLong;

/**
 * @author sudhakar
 * @created 26-Oct-2018
 */
public class WTStepFinish {
    public final boolean onOutsideClick;
    public final long onWait;
    public final boolean onElClick;
    public final boolean onElLongClick;
    public final boolean onNextAvailable;

    public WTStepFinish(List<Map<String, Object>> finishOnList) {
        boolean clickOutside = false;
        boolean click = false;
        boolean longClick = false;
        boolean nextAvailable = false;
        long wait = -1L;

        for(Map<String, Object> map: finishOnList) {
            String type = (String) map.get("type");
            Long value = toLong(map, "value", -1);

            if (type == null) type = "";

            switch (type) {
                case "clickOutside":
                    clickOutside = true;
                    break;
                case "wait":
                    wait = value;
                    break;
                case "click":
                    click = true;
                    break;
                case "longClick":
                    longClick = true;
                    break;
                case "nextAvailable":
                    nextAvailable = true;
                    break;
            }
        }

        this.onOutsideClick = clickOutside;
        this.onWait = wait;
        this.onElClick = click;
        this.onElLongClick = longClick;
        this.onNextAvailable = nextAvailable;
    }
}
