package com.useriq.demo;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ClippedRectActivity extends AppCompatActivity implements View.OnClickListener {

    private FrameLayout container;
    TextView textView;
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clipped_rect);

        container = findViewById(R.id.fl_container);

        findViewById(R.id.clContainer).setOnClickListener(this);

        textView = new TextView(this);
        textView.setOnClickListener(this);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp1.gravity = Gravity.CENTER;
        textView.setLayoutParams(lp1);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(25);

        ll = new LinearLayout(this);
        ll.setBackgroundColor(Color.YELLOW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ll.setElevation(10f);
        }
        ll.addView(textView);
    }

    @Override
    public void onClick(View v) {
        if (container.getChildCount() > 0) {
            container.removeAllViews();
        }

        switch (v.getId()) {
            case R.id.button_left: {
                LayoutParams lp = new LayoutParams(UnitUtil.dpToPx(250), LayoutParams.MATCH_PARENT);
                ll.setLayoutParams(lp);
                textView.setText("Left View");
                container.addView(ll);
                break;
            }
            case R.id.button_top: {
                LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, UnitUtil.dpToPx(250));
                ll.setLayoutParams(lp);
                textView.setText("Top View");
                container.addView(ll);
                break;
            }
            case R.id.button_right: {
                LayoutParams lp = new LayoutParams(UnitUtil.dpToPx(250), LayoutParams.MATCH_PARENT);
                lp.gravity = Gravity.RIGHT;
                ll.setLayoutParams(lp);
                textView.setText("Right View");
                container.addView(ll);
                break;
            }
            case R.id.button_bottom: {
                LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UnitUtil.dpToPx(250));
                lp.gravity = Gravity.BOTTOM;
                ll.setLayoutParams(lp);
                textView.setText("Bottom View");
                container.addView(ll);
                break;
            }
            case R.id.button_top_left: {
                LayoutParams lp = new LayoutParams(UnitUtil.dpToPx(280), UnitUtil.dpToPx(280));
                ll.setLayoutParams(lp);
                textView.setText("Top Left View");
                container.addView(ll);
                break;
            }
            case R.id.button_top_right: {
                LayoutParams lp = new LayoutParams(UnitUtil.dpToPx(280), UnitUtil.dpToPx(280));
                lp.gravity = Gravity.RIGHT;
                ll.setLayoutParams(lp);
                textView.setText("Top Right View");
                container.addView(ll);
                break;
            }
            case R.id.button_bottom_left: {
                LayoutParams lp = new LayoutParams(UnitUtil.dpToPx(280), UnitUtil.dpToPx(280));
                lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
                ll.setLayoutParams(lp);
                textView.setText("Bottom Left View");
                container.addView(ll);
                break;
            }
            case R.id.button_bottom_right: {
                LayoutParams lp = new LayoutParams(UnitUtil.dpToPx(280), UnitUtil.dpToPx(280));
                lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
                ll.setLayoutParams(lp);
                textView.setText("Bottom Right View");
                container.addView(ll);
                break;
            }
            case R.id.button_centre: {
                LayoutParams lp = new LayoutParams(UnitUtil.dpToPx(280), UnitUtil.dpToPx(280));
                lp.gravity = Gravity.CENTER;
                ll.setLayoutParams(lp);
                textView.setText("Centre View");
                container.addView(ll);
                break;
            }
        }
    }
}
