package com.useriq.sdk.helpcenter.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ListView;
import android.widget.TextView;

import static com.useriq.sdk.util.UnitUtil.dpToPx;

/**
 * @author sudhakar
 * @created 23-Oct-2018
 */
public class QuestionAnswerView extends ListView {
    private final TextView header;

    public QuestionAnswerView(Context context) {
        this(context, null);
    }

    public QuestionAnswerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuestionAnswerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setId(android.R.id.list);

        header = new TextView(context);
        header.setTextColor(getResources().getColor(android.R.color.black));
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        header.setPadding(dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20));

        addHeaderView(header, null, false);
    }

    public void setHeaderText(String text) {
        header.setText(text);
    }
}
