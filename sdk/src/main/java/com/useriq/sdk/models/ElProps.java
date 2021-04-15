package com.useriq.sdk.models;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import java.util.Map;

import static com.useriq.sdk.util.Utils.toColor;

/**
 * @author sudhakar
 * @created 30-Sep-2018
 */
public class ElProps {
    /**
     * wIndex - Reversed window index, where this element is found
     */
    public final int wIndex;

    /**
     * text - text of the view
     */
    public final String text;

    /**
     * class - Node's class like android.widget.Button
     */
    public final String cls;

    /**
     * bgColor: Background color of node
     */
    public final Integer bgColor;

    /**
     * id: id for view given by devs (com.some.app:id/layout)
     */
    private final String id;

    /**
     * sysId: (android:id)
     */
    private final String sysId;

    /**
     * contentDesc: contentDesc of the view
     * In some cases (esp Xamarin), id is saved to contentDesc opposed to resourceId.
     * contentDesc is usually good enough to uniquely identify the element
     */
    public final CharSequence contentDesc;

    /**
     * intId: computed int ID of the view
     */
    private Integer intId;

    /**
     * testID: testID for the view given by react native devs
     */
    public final String testID;

    /**
     * clickable: is view clickable
     */
    public final Boolean clickable;

    /**
     * longClickable: is view long clickable
     */
    public final Boolean longClickable;

    /**
     * enabled: is view enabled
     */
    public final Boolean enabled;

    /**
     * isVisible: is view visible
     */
    public final Boolean isVisible;

    ElProps(Map<String, Object> map) {
        this.id = (String) map.get("id");
        this.sysId = (String) map.get("sysId");
        this.bgColor = map.containsKey("bgColor") ? toColor(map, "bgColor", Color.RED) : null ;
        this.wIndex = map.containsKey("wIndex")
                ? ((Long) map.get("wIndex")).intValue() : 0;
        this.cls = (String) map.get("cls");
        this.text = (String) map.get("text");
        this.testID = (String) map.get("testID");
        this.contentDesc = (String) map.get("contentDesc");
        this.clickable = (Boolean) map.get("clickable");
        this.longClickable = (Boolean) map.get("longClickable");
        this.enabled = (Boolean) map.get("enabled");
        this.isVisible = (Boolean) map.get("isVisible");
    }

    private Integer getID(Context ctx) {
        String myId = id != null ? id : sysId;
        if (myId != null && intId == null) {
            String defPackage = id != null ? ctx.getPackageName() : "android";
            intId = ctx.getResources().getIdentifier(myId, "id", defPackage);
        }
        return intId;
    }

    boolean matchesID(View target) {
        if (id == null && sysId == null) return true;
        return getID(target.getContext()) == target.getId();
    }
}
