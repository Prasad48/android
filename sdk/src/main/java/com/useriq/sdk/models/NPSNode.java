package com.useriq.sdk.models;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.useriq.sdk.util.UnitUtil;
import com.useriq.sdk.v1Modal.CustomLayout;

import java.util.Map;

import static com.useriq.sdk.util.Utils.toColor;

public class NPSNode extends UINode {

    private int activeColor;
    private int cornerRadius = 100;
    private int numStart;
    private int numEnd;
    private TextView selectedButton;
    private boolean isNPSDefault;

    private static final String KEY_ACTIVE_COLOR = "activeColor";
    private static final String KEY_NUM_START = "numStart";
    private static final String KEY_NUM_END = "numEnd";

    public NPSNode(Map map) {
        type = NPS;
        if (map.containsKey(KEY_ACTIVE_COLOR)) {
            this.activeColor = toColor(map, KEY_ACTIVE_COLOR, Color.RED);
        }
        if (map.containsKey(KEY_NUM_START)) {
            this.numStart = ((Long) map.get(KEY_NUM_START)).intValue();
        }
        if (map.containsKey(KEY_NUM_END)) {
            this.numEnd = ((Long) map.get(KEY_NUM_END)).intValue();
        }
    }

    @NonNull
    public View buildView(Context context, CustomLayout.ClickHandler clickHandler, V1Modal modal) {
        if (modal.type.equals(V1Modal.MODAL_TYPE_npsDefault)) {
            isNPSDefault = true;
        }
        LinearLayout llParent = new LinearLayout(context);
        llParent.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        llParent.setLayoutParams(lp);
        llParent.setPadding(0, 0, 0, 0);
        llParent.setGravity(Gravity.CENTER);

        int totalNode = numEnd - numStart + 1;
        int numOfRows = (totalNode / 6);
        if (totalNode % 6 != 0) numOfRows++;

        int num = numStart;
        for (int i = 0; i < numOfRows; i++) {
            LinearLayout row = new LinearLayout(context);
            row.setGravity(Gravity.CENTER);

            for (int j = 0; j <= 5 && num <= numEnd; j++) {
                row.addView(getButton(context, num++, clickHandler));
            }
            llParent.addView(row);
        }

        return llParent;
    }

    private TextView getButton(Context context, final int num, final CustomLayout.ClickHandler clickHandler) {
        TextView tv = new TextView(context);
        tv.setText(String.valueOf(num));
        String id = "tv" + num;
        tv.setId(id.hashCode());
        tv.setTextColor(activeColor);
        if (isNPSDefault) tv.setTextSize(12);
        else tv.setTextSize(15);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (isNPSDefault)
            lp.setMargins(UnitUtil.dpToPx(5), UnitUtil.dpToPx(5), UnitUtil.dpToPx(5), UnitUtil.dpToPx(5));
        else
            lp.setMargins(UnitUtil.dpToPx(8), UnitUtil.dpToPx(7), UnitUtil.dpToPx(8), UnitUtil.dpToPx(5));
        tv.setLayoutParams(lp);
        tv.setGravity(Gravity.CENTER);
        if (num >= 10) {
            if (isNPSDefault)
                tv.setPadding(UnitUtil.dpToPx(10), UnitUtil.dpToPx(9), UnitUtil.dpToPx(10), UnitUtil.dpToPx(9));
            else
                tv.setPadding(UnitUtil.dpToPx(11), UnitUtil.dpToPx(9), UnitUtil.dpToPx(11), UnitUtil.dpToPx(9));
        } else {
            if (isNPSDefault)
                tv.setPadding(UnitUtil.dpToPx(13), UnitUtil.dpToPx(9), UnitUtil.dpToPx(13), UnitUtil.dpToPx(9));
            else
                tv.setPadding(UnitUtil.dpToPx(15), UnitUtil.dpToPx(9), UnitUtil.dpToPx(15), UnitUtil.dpToPx(9));
        }
        tv.setBackground(getBackgroundDrawable(false));
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickHandler.onNpsClick(String.valueOf(num));
                if (selectedButton != null) {
                    selectedButton.setBackground(getBackgroundDrawable(false));
                    selectedButton.setTextColor(activeColor);
                }
                selectedButton = (TextView) v;
                v.setBackground(getBackgroundDrawable(true));
                ((TextView) v).setTextColor(Color.argb(bgColor.get(0), bgColor.get(1), bgColor.get(2), bgColor.get(3)));
            }
        });
        return tv;
    }

    private Drawable getBackgroundDrawable(boolean forSelectedButton) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(cornerRadius);
        if (forSelectedButton) {
            gradientDrawable.setColor(activeColor);
        } else {
            gradientDrawable.setColor(Color.argb(bgColor.get(0), bgColor.get(1), bgColor.get(2), bgColor.get(3)));
        }
        gradientDrawable.setStroke((int) 2, Color.argb(255, 211, 211, 211));
        gradientDrawable.setCornerRadii(new float[]{100, 100, 100, 100, 100, 100, 100, 100});
        return gradientDrawable;
    }

}