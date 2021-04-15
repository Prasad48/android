package com.useriq.sdk.models;


import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;

import com.useriq.sdk.v1Modal.CustomLayout;
import com.useriq.sdk.views.StarRatingView;

import java.util.Map;

import static com.useriq.sdk.util.Utils.toColor;

public class RatingNode extends UINode {

    private int rating;
    private int numOfStar;
    private int activeColor;
    private int starBgColor;
    private int starBorderWidth;
    private int starBorderColor;

    private static final String KEY_NUM_STARS = "numStars";
    private static final String KEY_ACTIVE_COLOR = "activeColor";
    private static final String KEY_BORDER_COLOR = "borderColor";
    private static final String KEY_BORDER_WIDTH = "borderWidth";
    private static final String KEY_BG_COLOR = "bgColor";

    RatingNode(Map map) {
        type = RATING;
        if (map.containsKey(KEY_NUM_STARS)) {
            this.numOfStar = ((Long) map.get(KEY_NUM_STARS)).intValue();
        }
        if (map.containsKey(KEY_ACTIVE_COLOR)) {
            this.activeColor = toColor(map, KEY_ACTIVE_COLOR, Color.RED);
        }
        if (map.containsKey(KEY_BORDER_COLOR)) {
            this.starBorderColor = toColor(map, KEY_BORDER_COLOR, Color.RED);
        }
        if (map.containsKey(KEY_BORDER_WIDTH)) {
            this.starBorderWidth = ((Long) map.get(KEY_BORDER_WIDTH)).intValue();
        }
        if (map.containsKey(KEY_BG_COLOR)) {
            this.starBgColor = toColor(map, KEY_BG_COLOR, Color.WHITE);
        }
    }

    @NonNull
    public View buildView(Context context, final CustomLayout.ClickHandler clickHandler) {
        StarRatingView starRating = new StarRatingView(context);
        if (id != null) {
            starRating.setId(id.hashCode());
        }
        starRating.setStarNum(numOfStar);
        starRating.setStarForegroundColor(activeColor);
        starRating.setBorderColor(starBorderColor);
        starRating.setBorderWidth(starBorderWidth);
        starRating.setStarBackgroundColor(starBgColor);
        starRating.setOnClickListener(new StarRatingView.MyOnClickListener() {
            @Override
            public void onRatingChange(int rating) {
                clickHandler.onStarRatingClick(String.valueOf(rating));
            }
        });

        return starRating;
    }
}