package com.useriq.sdk.ctxHelp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.useriq.Logger;
import com.useriq.sdk.BuildConfig;
import com.useriq.sdk.IconView;
import com.useriq.sdk.R;
import com.useriq.sdk.UIManager;
import com.useriq.sdk.UIRouter;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.capture.ViewRoot;
import com.useriq.sdk.fonticon.UnfoldFontIcon;
import com.useriq.sdk.helpcenter.AnswerWithToolbarCtrl;
import com.useriq.sdk.helpcenter.HelpCenterCtrl;
import com.useriq.sdk.models.CtxHelp;
import com.useriq.sdk.models.CtxHelpItem;
import com.useriq.sdk.models.Question;
import com.useriq.sdk.models.Screen;
import com.useriq.sdk.models.SyncData;
import com.useriq.sdk.models.Theme;
import com.useriq.sdk.models.Walkthrough;
import com.useriq.sdk.walkthrough.WalkthroughCtrl;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.useriq.sdk.util.UnitUtil.dpToPx;

/**
 * @author sudhakar
 * @created 23-Oct-2018
 */
public class CtxHelpCtrl implements UIRouter.Controller {
    private static final Logger logger = Logger.init(CtxHelpCtrl.class.getSimpleName());

    private final Context ctx;
    private FrameLayout myRoot;
    private final String ctxId;
    private LinearLayout helpScrollerBox;
    private TextView moreBtn;

    public CtxHelpCtrl() {
        this(null);
    }

    public CtxHelpCtrl(String ctxId) {
        this.ctxId = ctxId;
        this.ctx = UserIQSDKInternal.getContext();
    }

    @Override
    public View onEnter() {
        if (UIManager.getInstance().getUiRootView().getRootType() != ViewRoot.ACTIVITY) {
            Toast.makeText(UserIQSDKInternal.getApp(), "Close the dialog to see Contextual Help", Toast.LENGTH_LONG).show();
            return null;
        }
        UserIQSDKInternal.getAnalyticsManager().onCtxHelp(true);
        Screen currScreen = UserIQSDKInternal.getCurrScreen();
        SyncData syncData = UserIQSDKInternal.getSyncData();

        this.myRoot = buildView();

        if (syncData == null) return myRoot;

        CtxHelp helpForScreen = null;

        if (ctxId != null) { // from ui-web
            helpForScreen = syncData.getCtxHelpById(ctxId);
            hydrate(helpForScreen);
            return myRoot;
        }

        if (currScreen != null) { // from FAB
            helpForScreen = syncData.getCtxHelpForScreen(currScreen.id);
        }

        if (helpForScreen == null || helpForScreen.entries.isEmpty()) {
            UIRouter.getInstance().pop();
            logger.d("no ctxHelp. popping off to prev state!");
            return null;
        }

        hydrate(helpForScreen);

        return myRoot;
    }

    @Override
    public void onExit() {
        UserIQSDKInternal.getAnalyticsManager().onCtxHelp(false);
    }

    @Override
    public boolean onBackPressed() {
        UIRouter.getInstance().pop();
        return true;
    }

    /**
     * hydrate CtxHelp views with real data
     */
    private void hydrate(CtxHelp ctxHelp) {
        helpScrollerBox.removeAllViews();

        SyncData syncData = UserIQSDKInternal.getSyncData();
        applyTheme(syncData.theme);

        if (ctxHelp == null) {
            logger.d("no ctxHelp. Not hydrating!");
            return;
        }

        for (CtxHelpItem helpItem : ctxHelp.entries) {
            switch (helpItem.type) {
                case walkthrough: {
                    final Walkthrough wt = syncData.getWalkthroughById(helpItem.id);
                    if (wt == null) {
                        logger.w("hydrate(): Walkthrough not found for id " + helpItem.id);
                        continue;
                    }
                    LinearLayout row = buildItem("i", wt.name, syncData.theme);
                    helpScrollerBox.addView(row);
                    helpScrollerBox.addView(buildDivider());
                    row.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            if (SDK_INT < JELLY_BEAN_MR2) {
                                String msg = "Walkthrough is not supported in API_LEVEL=" + SDK_INT;
                                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
                                logger.w(msg);
                                return;
                            }
                            UIRouter.getInstance().pop();
                            UIRouter.getInstance().push(new WalkthroughCtrl(wt.id));
                        }
                    });
                    break;
                }
                case question: {
                    final Question qn = syncData.getQuestionById(helpItem.id);
                    if (qn == null) {
                        logger.w("hydrate(): Question not found for id " + helpItem.id);
                        continue;
                    }
                    LinearLayout row = buildItem("k", qn.name, syncData.theme);
                    helpScrollerBox.addView(row);
                    helpScrollerBox.addView(buildDivider());
                    row.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            UIRouter.getInstance().push(new AnswerWithToolbarCtrl(qn.id));
                        }
                    });
                    break;
                }
            }
        }
        if (ctxHelp.entries.size() == 0) {
            Typeface iconFont = UnfoldFontIcon.getTypeface(ctx);
            TextView blankPlaceholder = new TextView(ctx);
            blankPlaceholder.setText(ctx.getString(R.string.uiq_ic_blank_image));
            blankPlaceholder.setTextColor(ctx.getResources().getColor(R.color.empty_state_grey));
            blankPlaceholder.setGravity(Gravity.CENTER);
            blankPlaceholder.setTextSize(150);
            blankPlaceholder.setPadding(0,30,0,0);
            blankPlaceholder.setTypeface(iconFont);
            helpScrollerBox.addView(blankPlaceholder);
        }
    }

    private FrameLayout buildView() {
        LayoutInflater li = LayoutInflater.from(ctx);

        FrameLayout holderFrame = new FrameLayout(ctx);
        holderFrame.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        FrameLayout backDrop = new FrameLayout(ctx);
        backDrop.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        Resources res = ctx.getResources();
        backDrop.setBackgroundColor(res.getColor(R.color.containerBackdrop));

        LinearLayout ctxHelp = (LinearLayout) li.inflate(R.layout.uiq_ctx_help, holderFrame, false);

        ScrollView scrollView = ctxHelp.findViewById(R.id.ctxHelpScroller);

        helpScrollerBox = new LinearLayout(ctx);
        helpScrollerBox.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        helpScrollerBox.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(helpScrollerBox);

        moreBtn = ctxHelp.findViewById(R.id.moreBtn);

        moreBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UIRouter.getInstance().push(new HelpCenterCtrl());
            }
        });

        ((TextView)ctxHelp.findViewById(R.id.textBuildVersion)).setText("UserIQ SDK - v" + BuildConfig.VERSION_NAME);

        backDrop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

        holderFrame.addView(backDrop);
        holderFrame.addView(ctxHelp);

        int dialogBgColor = res.getColor(R.color.ctxBg);
        int dialogRadius = res.getDimensionPixelSize(R.dimen.dialog_corners);

        ctxHelp.setBackground(getCtxHelpBg(dialogRadius, dialogBgColor));

        if (SDK_INT >= LOLLIPOP) {
            backDrop.setTransitionName(res.getString(R.string.transition_fade_view));

            ctxHelp.setTransitionName(res.getString(R.string.transition_morph_view));
            ctxHelp.setTag(R.id.viewBgColor, dialogBgColor);
            ctxHelp.setTag(R.id.viewRadius, dialogRadius);
        }

        return holderFrame;
    }

    /**
     * Provides the the textview with "more" text
     */
    private void applyTheme(Theme theme) {
        Resources res = ctx.getResources();

        TextView title = myRoot.findViewById(R.id.ctxHelpTitle);
        TextView subTitle = myRoot.findViewById(R.id.ctxHelpSubTitle);
        TextView buildTxt = myRoot.findViewById(R.id.textBuildVersion);

        boolean isDarkBg = isDarkColor(theme.ctxBgColor);

        title.setTextColor(darkenColor(theme.ctxColor, isDarkBg ? .8f : 1.2f));
        subTitle.setTextColor(darkenColor(theme.ctxColor, isDarkBg? 1.2f : .8f));
        buildTxt.setTextColor(darkenColor(theme.ctxColor, isDarkBg? 1.2f : .8f));

        moreBtn.setTextColor(theme.ctxBtnColor);
        moreBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimension(R.dimen.appunfold_contextual_more_text_size));

        Drawable bg = getMoreItemBg(theme);
        InsetDrawable insetBg = new InsetDrawable(bg, 0, 12, 0, 12);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            moreBtn.setBackground(insetBg);
        } else {
            moreBtn.setBackgroundDrawable(insetBg);
        }
        int hPadding = (int) res.getDimension(R.dimen.appunfold_contextual_more_l_r_padding);
        moreBtn.setPadding(hPadding, 0,hPadding,0);

        int dialogRadius = res.getDimensionPixelSize(R.dimen.dialog_corners);

        View ctxHelp = myRoot.findViewById(R.id.ctxHelp);
        ctxHelp.setBackground(getCtxHelpBg(dialogRadius, theme.ctxBgColor));
        ctxHelp.setTag(R.id.viewBgColor, theme.ctxBgColor);
    }

    private Drawable getCtxHelpBg(int dialogRadius, int dialogBgColor) {
        GradientDrawable gradientDrawable=new GradientDrawable();
        gradientDrawable.setCornerRadius(dialogRadius);
        gradientDrawable.setColor(dialogBgColor);

        return gradientDrawable;
    }

    /**
     * Provides the background for textview with "more" text
     *
     * @return Drawable
     * @param theme
     */
    private Drawable getMoreItemBg(Theme theme) {
        Drawable drawable = getRoundedRectCtxColorStroke(theme);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(getRippleColor(), drawable, drawable);
        }
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new LayerDrawable(new Drawable[]{
                drawable,
                getRoundedSelectedRect()
        }));
        stateListDrawable.addState(new int[]{}, drawable);
        return stateListDrawable;
    }

    private GradientDrawable getRoundedRectCtxColorStroke(Theme theme) {
        Resources res = ctx.getResources();
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.TRANSPARENT);
        gradientDrawable.setAlpha(230);

        float dimension = res.getDimension(R.dimen.appunfold_contextual_more_radius);
        gradientDrawable.setCornerRadius(dimension);
        gradientDrawable.setStroke(res.getDimensionPixelSize(R.dimen.appunfold_contextual_more_stroke_width),
                theme.ctxBtnBorderColor
        );
        return gradientDrawable;
    }

    private ShapeDrawable getRoundedSelectedRect() {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
        float dimension = ctx.getResources().getDimension(R.dimen.appunfold_contextual_more_radius);
        RoundRectShape round = new RoundRectShape(new float[]{dimension, dimension, dimension, dimension, dimension, dimension, dimension, dimension}, null, null);
        shapeDrawable.setShape(round);
        shapeDrawable.setAlpha(102);
        Paint paint = shapeDrawable.getPaint();
        paint.setColor(Color.BLACK);
        return shapeDrawable;
    }

    private ColorStateList getRippleColor() {
        int pressedColor = Color.argb(102, 0, 0, 0);
        return new ColorStateList(
                new int[][]{ new int[]{} },
                new int[]{ pressedColor }
        );
    }

    private LinearLayout buildItem(String icon, String label, Theme theme) {
        LinearLayout layout = new LinearLayout(ctx);
        MarginLayoutParams lp = new MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(dpToPx(20), dpToPx(10), dpToPx(20), dpToPx(10));
        layout.setClickable(true);
        layout.setBackground(ctx.getResources().getDrawable(R.drawable.appunfold_selectable_item_background));
        layout.setLayoutParams(lp);

        IconView iconView = new IconView(ctx);
        iconView.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        iconView.setText(icon);
        iconView.setTextSize(15);
        iconView.setTextColor(theme.ctxColor);
        LinearLayout.LayoutParams ivlp = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        ivlp.gravity = Gravity.CENTER_VERTICAL;
        ivlp.setMargins(dpToPx(0), dpToPx(0), dpToPx(10), dpToPx(0));
        layout.addView(iconView, ivlp);

        TextView textView = new TextView(ctx);
        textView.setText(label);
        textView.setTextSize(15);
        textView.setSingleLine();
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextColor(theme.ctxColor);
        LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        tvlp.gravity = Gravity.CENTER_VERTICAL;
        tvlp.weight = 1;
        layout.addView(textView, ivlp);

        return layout;
    }

    private View buildDivider() {
        View divider = new View(ctx);
        divider.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, 1));
        divider.setBackgroundColor(ctx.getResources().getColor(R.color.divider_grey));
        return divider;
    }

    @ColorInt
    private int darkenColor(@ColorInt int color, float factor) {
        int alpha = Color.alpha(color);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor;
        return Color.HSVToColor(alpha, hsv);
    }

    private boolean isDarkColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        return hsv[2] < 0.5;
    }
}
