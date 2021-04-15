package com.useriq.sdk.ctxHelp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.useriq.sdk.R;
import com.useriq.sdk.SDKConfig;
import com.useriq.sdk.UIManager;
import com.useriq.sdk.UIRootView;
import com.useriq.sdk.UIRouter;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.capture.ViewRoot;
import com.useriq.sdk.helpcenter.HelpCenterCtrl;
import com.useriq.sdk.models.CtxHelp;
import com.useriq.sdk.models.Screen;
import com.useriq.sdk.models.SyncData;
import com.useriq.sdk.models.Theme;
import com.useriq.sdk.util.UnitUtil;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.useriq.sdk.util.ScreenBoundsUtil.getScreenBoundWithoutNav;
import static com.useriq.sdk.util.UnitUtil.spToPx;

/**
 * @author sudhakar
 * @created 23-Oct-2018
 */
public class FABCtrl implements UIRouter.Controller {
    // Activate Delay is required because the campaign was flickering(opening and closing instantly)
    // when the delay is near to 0. No idea what is causing this. Delay value is tested down to 100ms but
    // setting it to 300ms for now. */
    public static final int ACTIVATE_DELAY = 300;
    private final FrameLayout myRoot;
    private final Context ctx;
    private Button fabBtn;
    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private int origInputMode = -1;

    public FABCtrl() {
        this.ctx = UserIQSDKInternal.getContext();
        this.myRoot = buildFabHolder();
        this.fabBtn = buildFab();

        fabBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UserIQSDKInternal.getAnalyticsManager().onFabClick();
                Screen currScreen = UserIQSDKInternal.getCurrScreen();

                CtxHelp ctxHelp = null;
                if (currScreen != null) {
                    ctxHelp = UserIQSDKInternal.getSyncData().getCtxHelpForScreen(currScreen.id);
                }

                UIRouter.getInstance().push(
                        ctxHelp != null ? new CtxHelpCtrl() : new HelpCenterCtrl());
            }
        });
    }

    @Override
    public View onEnter() {
        final UIRootView uiRootView = UIManager.getInstance().getUiRootView();

        if (origInputMode != -1 && UserIQSDKInternal.getCurrActivity() != null) {
            Window window = UserIQSDKInternal.getCurrActivity().getWindow();
            window.setSoftInputMode(this.origInputMode);
        }

        uiRootView.setCallback(new UIRootView.Callback() {
            @Override
            public void onDetached() {
                myRoot.removeView(fabBtn);
            }

            @Override
            public void onAttached() {
                attach();
            }

        });

        attach();

        applyNextDelayed();

        return myRoot;
    }

    @Override
    public void onExit() {
        if (UserIQSDKInternal.getCurrActivity() != null) {
            Window window = UserIQSDKInternal.getCurrActivity().getWindow();
            this.origInputMode = window.getAttributes().softInputMode;
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        uiThreadHandler.removeCallbacksAndMessages(null);
        UIManager.getInstance().getUiRootView().setCallback(null);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    public void applyNextDelayed() {
        uiThreadHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                uiThreadHandler.removeCallbacksAndMessages(null);
                UIManager.getInstance().applyNext();
            }
        }, ACTIVATE_DELAY);
    }

    private void attach() {
        final UIRootView uiRootView = UIManager.getInstance().getUiRootView();

        boolean showFab = uiRootView.getRootType() == ViewRoot.ACTIVITY
                && fabBtn.getParent() != myRoot
                && isFABEnabled();

        if (showFab) {
            final Rect screenBounds = getScreenBoundWithoutNav(ctx);

            int fabHelpTextWidth = UnitUtil.dpToPx(44);
            int fabHelpTextHeight = UnitUtil.dpToPx(44);
            int fabCircularWidth = UnitUtil.dpToPx(44);
            int fabCircularHeight = UnitUtil.dpToPx(44);

            int fabHelpTextXPos = screenBounds.right - fabHelpTextWidth;
            int fabHelpTextYPos = (int) (screenBounds.bottom * .58);
            int fabCircleXPos = fabHelpTextXPos + (fabHelpTextWidth - fabCircularWidth / 2);
            int fabCircleYPos = fabHelpTextYPos;

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(fabCircularWidth, fabCircularHeight);
            lp.leftMargin = fabCircleXPos;
            lp.topMargin = fabCircleYPos;

            fabBtn.setLayoutParams(lp);

            myRoot.removeAllViews();
            myRoot.addView(fabBtn);
        }

        applyNextDelayed();
    }

    private boolean isFABEnabled() {
        SyncData syncData = UserIQSDKInternal.getSyncData();
        SDKConfig sdkConfig = UserIQSDKInternal.getSDKConfig();

        return !sdkConfig.fabDisabled && syncData.fabEnabled;
    }

    private FrameLayout buildFabHolder() {
        FrameLayout fabHolder = new FrameLayout(ctx);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        fabHolder.setLayoutParams(lp);
        return fabHolder;
    }

    private Button buildFab() {

        Button btn = new Button(ctx);
        Theme theme = UserIQSDKInternal.getSyncData().theme;

        btn.setTypeface(null, Typeface.BOLD);

        btn.setGravity(Gravity.CENTER_VERTICAL);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        btn.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        btn.setText("?");
        btn.setTextColor(theme.fabColor);
        btn.setPadding(spToPx(10), 0, 0, 0);

        if (SDK_INT >= JELLY_BEAN_MR1) {
            btn.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        } else {
            btn.setText("  ?");
        }

        // FIXME: Doesnt seem to work!
        if (SDK_INT >= LOLLIPOP) {
            btn.setElevation(20);
        }

        Drawable drawable = getCircularBgDrawable(theme);

        if (SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            btn.setBackground(drawable);
        } else {
            btn.setBackgroundDrawable(drawable);
        }

        if (SDK_INT >= LOLLIPOP) {
            btn.setTransitionName(ctx.getResources().getString(R.string.transition_morph_view));
            btn.setTag(R.id.viewBgColor, theme.fabBgColor);
            btn.setTag(R.id.viewRadius, ctx.getResources().getDimensionPixelSize(R.dimen.fab_radius));
        }

        return btn;
    }

    private static Drawable getCircularBgDrawable(Theme theme) {
        int pressedColor = Color.argb(50, 0, 0, 0);
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(theme.fabBgColor);

        if (SDK_INT >= LOLLIPOP) {
            ColorStateList rippleColor = new ColorStateList(
                    new int[][]{new int[]{}},
                    new int[]{pressedColor}
            );

            return new RippleDrawable(rippleColor, shapeDrawable, shapeDrawable);
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new LayerDrawable(new Drawable[]{
                    shapeDrawable,
                    new ColorDrawable(pressedColor)
            }));
            stateListDrawable.addState(new int[]{}, shapeDrawable);
            return stateListDrawable;
        }
    }

}
