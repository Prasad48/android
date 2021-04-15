package com.useriq.sdk.screentour;

import android.animation.Animator;
import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.useriq.Logger;
import com.useriq.sdk.MatchFinder;
import com.useriq.sdk.R;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.models.STStep;
import com.useriq.sdk.models.ScreenTour;
import com.useriq.sdk.util.PathUtil;
import com.useriq.sdk.util.ScreenBoundsUtil;
import com.useriq.sdk.util.UnitUtil;

import java.util.ArrayList;


public class ScreenTourView extends ShowcaseView {
    private static final Logger logger = Logger.init(ScreenTourView.class.getSimpleName());

    int mCurrentStep;
    ArrayList<STStep> mStSteps;
    View mActionView;
    TextView mDescTextView;
    Button mDismissBtn;
    String mId;
    Application mContext;

    private ScreenTourView(@NonNull Application context, ArrayList<Element> elements, int paddingFromTarget, @ColorInt int bgColor, @ColorInt int borderColor, @ColorInt int textColor, int textSize, ArrayList<STStep> stSteps, String id) {
        super(context, elements, paddingFromTarget, bgColor, borderColor, false);
        mActionView = LayoutInflater.from(UserIQSDKInternal.getContext()).inflate(R.layout.appunfold_screen_tour_action, null);
        mContext = context;
        mDescTextView = (TextView) mActionView.findViewById(R.id.tv_desc);
        mDescTextView.setTextColor(textColor);
        mDismissBtn = (Button) mActionView.findViewById(R.id.btn_dismiss);
        mDismissBtn.setTextColor(textColor);
        mStSteps = stSteps;
        mDismissBtn.setTextSize(20);
        mId = id;
        // textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, context.getResources().getDisplayMetrics());
        if (textSize != 0) {
            //mDismissBtn.setTextSize(textSize);
            mDescTextView.setTextSize(textSize);
        }
        addView(mActionView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int closeSize = UnitUtil.dpToPx(22);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(closeSize, closeSize);
        layoutParams.gravity = Gravity.END;
        layoutParams.topMargin = ScreenBoundsUtil.getStatusHeight(UserIQSDKInternal.getContext());
        addView(getCloseTextView(), layoutParams);
        updateActionView();
        animateInView();
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //  mCurrentStep = mStSteps.size() - 1;
                // animateOutView();
                return true;
            }
        });
    }

    public static ScreenTourView start(final Activity activity, final ScreenTour screenTour) {

        ArrayList<Element> elements = new ArrayList<>();
        View targetView = null;
        int i;
        for (i = 0; i < screenTour.steps.size(); i++) {
            targetView = MatchFinder.findView(activity, screenTour.steps.get(i).element);
            if (targetView != null) {
                break;
            } else {
                logger.d(screenTour.steps.get(i).element.id + " view not found");
            }
        }
        if (targetView == null) {
            Toast.makeText(UserIQSDKInternal.getContext(), "No view found", Toast.LENGTH_SHORT).show();
            return null;
        }
        Rect inset = PathUtil.getPositionForView(targetView);
        elements.add(new Element(Element.RECT, inset, screenTour.steps.get(0).desc));
        /*ScreenTourView screenTourView = new ScreenTourView(activity,
                elements,
                UserIQSDKInternal.getInstance().getResources().getDimensionPixelOffset(R.dimen.target_padding),
                Color.parseColor("#CC000011"),
                screenTour.stSteps
        );*/
        final ScreenTourView screenTourView;
        if (screenTour.theme != null) {
            screenTourView = new ScreenTourView(activity.getApplication(),
                    elements,
                    UserIQSDKInternal.getInstance().getResources().getDimensionPixelOffset(R.dimen.appunfold_target_padding),
                    screenTour.theme.bgColor,
                    screenTour.theme.borderColor,
                    screenTour.theme.textColor,
                    screenTour.theme.textSize,
                    screenTour.steps,
                    screenTour.id
            );
        } else {
            screenTourView = new ScreenTourView(activity.getApplication(),
                    elements,
                    UserIQSDKInternal.getInstance().getResources().getDimensionPixelOffset(R.dimen.appunfold_target_padding),
                    Color.parseColor("#CC000011"),
                    Color.TRANSPARENT,
                    Color.WHITE,
                    0,
                    screenTour.steps,
                    screenTour.id
            );
        }
        screenTourView.mCurrentStep = i;
        UserIQSDKInternal.getAnalyticsManager().onTour(screenTour.id, screenTour.steps.get(0).id, true);
//        UIManager.getInstance().addViewToUxLayout(screenTourView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return screenTourView;
    }

    TextView getCloseTextView() {
        TextView tvClose = new TextView(mContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            tvClose.setBackground(getCloseBg());
        } else {
            tvClose.setBackgroundDrawable(getCloseBg());
        }
        tvClose.setTextSize(14);
        tvClose.setTypeface(null, Typeface.BOLD);
        tvClose.setGravity(Gravity.CENTER);
        tvClose.getPaint().setAntiAlias(true);
        tvClose.setText("\u2715");
        tvClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScreenTour();
            }
        });
        tvClose.setTextColor(Color.WHITE);
        return tvClose;
    }

    public GradientDrawable getCloseBg() {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.argb(153, 0, 0, 0));
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setStroke(UnitUtil.dpToPx(1), Color.argb(179, 0, 0, 0));
        return gradientDrawable;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int childCount = getChildCount();
        if (childCount == 0)
            return;
        Rect elementBounds = mElements.get(0).bounds;
        View view = getChildAt(0);
        if (elementBounds.top - getTop() > view.getMeasuredHeight()) {
            view.layout(0, elementBounds.top - view.getMeasuredHeight(),
                    view.getMeasuredWidth(),
                    elementBounds.top);
        } else if (getBottom() - elementBounds.bottom > view.getMeasuredHeight()) {
            view.layout(0, elementBounds.bottom,
                    view.getMeasuredWidth(),
                    elementBounds.bottom + view.getMeasuredHeight());
        }
    }

    void animateOutView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Animator animator = ViewAnimationUtils.createCircularReveal(ScreenTourView.this, mElements.get(0).bounds.centerX(), mElements.get(0).bounds.centerY(), getMeasuredHeight(), 0);
            animator.setDuration(400);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    handleNextView();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        } else {
            handleNextView();
        }

    }

    private void handleNextView() {
        setVisibility(INVISIBLE);
        UserIQSDKInternal.getAnalyticsManager().onTourStep(mId, mStSteps.get(mCurrentStep).id);
        if (mCurrentStep == mStSteps.size() - 1) {
            stopScreenTour();
            return;
        }
        mCurrentStep++;
        View targetView = MatchFinder.findView(UserIQSDKInternal.getCurrActivity(), mStSteps.get(mCurrentStep).element);
        if (targetView == null) {
            if (mCurrentStep == mStSteps.size() - 1) {
                Toast.makeText(UserIQSDKInternal.getContext(), "View not found", Toast.LENGTH_SHORT).show();
                stopScreenTour();
                return;
            } else {
                handleNextView();
                return;
            }
        }
        Rect inset = PathUtil.getPositionForView(targetView);
        mElements.clear();
        mElements.add(new Element(Element.RECT, inset, mStSteps.get(mCurrentStep).desc));
        mCanvas = null;
        requestLayout();
        updateActionView();
        animateInView();
    }

    public void stopScreenTour() {
//        UIManager.getInstance().setFab();
//        UIManager.getInstance().removeViewFromUxLayout(ScreenTourView.this);
        UserIQSDKInternal.getAnalyticsManager().onTour(mId, mStSteps.get(mCurrentStep).id, false);
    }

    void animateInView() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Animator animator = ViewAnimationUtils.createCircularReveal(ScreenTourView.this, mElements.get(0).bounds.centerX(), mElements.get(0).bounds.centerY(), 0, getMeasuredHeight());
                    animator.setDuration(400);
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            setVisibility(VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {

                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    animator.start();
                } else {
                    setVisibility(VISIBLE);
                }
            }
        });
    }


    private void updateActionView() {
        mDescTextView.setText(mElements.get(0).desc);
        if (mStSteps.size() - 1 > mCurrentStep) {
            mDismissBtn.setText(UserIQSDKInternal.getInstance().getResources().getString(R.string.appunfold_next));
        } else {
            mDismissBtn.setText(UserIQSDKInternal.getInstance().getResources().getString(R.string.appunfold_got_it));
        }
        mDismissBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                animateOutView();
            }
        });
    }
}
