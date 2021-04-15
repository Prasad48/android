package com.useriq.sdk.ctxHelp;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.useriq.sdk.R;

/**
 * @author sudhakar
 * @created 21-Oct-2018
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class UserIQMorphTransition extends ChangeBounds {
    private static final String PROPERTY_COLOR = "uiq:FabDialog:color";
    private static final String PROPERTY_CORNER_RADIUS = "uiq:FabDialog:cornerRadius";
    private static final String[] TRANSITION_PROPERTIES = {
            PROPERTY_COLOR,
            PROPERTY_CORNER_RADIUS
    };

    private final boolean isReversed;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static UserIQMorphTransition buildEnter(@ColorInt int startColor, int startRadius, @ColorInt int endColor, int endRadius) {
        return build(startColor, endColor, startRadius, endRadius, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static UserIQMorphTransition buildExit(@ColorInt int startColor, int startRadius, @ColorInt int endColor, int endRadius) {
        return build(startColor, endColor, startRadius, endRadius, true);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static UserIQMorphTransition build(@ColorInt int startColor, int startRadius, @ColorInt int endColor, int endRadius, boolean isReversed) {
        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(60f);
        arcMotion.setMinimumVerticalAngle(60f);

        UserIQMorphTransition transition = new UserIQMorphTransition(
                isReversed);
        transition.setPathMotion(arcMotion);
//        transition.addTarget(R.string.transition_morph_view);

        return transition;
    }

    public UserIQMorphTransition(boolean isReversed) {
        super();
        this.isReversed = isReversed;
    }

    @Override
    public String[] getTransitionProperties() {
        return TRANSITION_PROPERTIES;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        final View view = transitionValues.view;
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }

        Integer startColor = (Integer) view.getTag(R.id.viewBgColor);
        Integer startRadius = (Integer) view.getTag(R.id.viewRadius);

        transitionValues.values.put(PROPERTY_COLOR, startColor);
        transitionValues.values.put(PROPERTY_CORNER_RADIUS, startRadius);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        super.captureEndValues(transitionValues);
        final View view = transitionValues.view;
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }

        Integer endColor = (Integer) view.getTag(R.id.viewBgColor);
        Integer endRadius = (Integer) view.getTag(R.id.viewRadius);

        transitionValues.values.put(PROPERTY_COLOR, endColor);
        transitionValues.values.put(PROPERTY_CORNER_RADIUS, endRadius);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public Animator createAnimator(final ViewGroup sceneRoot,
                                   TransitionValues startValues,
                                   TransitionValues endValues) {
        Animator changeBounds = super.createAnimator(sceneRoot, startValues, endValues);
        if (startValues == null || endValues == null || changeBounds == null) {
            return null;
        }

        Integer startColor = (Integer) startValues.values.get(PROPERTY_COLOR);
        Integer endColor = (Integer) endValues.values.get(PROPERTY_COLOR);
        Integer startCornerRadius = (Integer) startValues.values.get(PROPERTY_CORNER_RADIUS);
        Integer endCornerRadius = (Integer) endValues.values.get(PROPERTY_CORNER_RADIUS);

        if (startColor == null || startCornerRadius == null || endColor == null ||
                endCornerRadius == null) {
            return null;
        }

        DecelerateInterpolator interpolator = new DecelerateInterpolator(2);

        // hide child views
        if (isReversed && startValues.view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) startValues.view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View v = vg.getChildAt(i);
                v.setAlpha(0);
            }
        }

        // ease in the child views
        if (!isReversed && endValues.view instanceof ViewGroup) {
            Interpolator ip = new DecelerateInterpolator();
            ViewGroup vg = (ViewGroup) endValues.view;
            float offset = vg.getHeight() / 4;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View v = vg.getChildAt(i);
                v.setTranslationY(offset);
                v.setAlpha(0f);
                v.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300L)
                        .setStartDelay(0L)
                        .setInterpolator(ip);
                offset *= 1.1f;
            }
        }

        MorphDrawable background = new MorphDrawable(startColor, startCornerRadius);
        endValues.view.setBackground(background);

        Animator color = ObjectAnimator.ofArgb(background, MorphDrawable.COLOR, endColor);
        Animator corners = ObjectAnimator.ofFloat(background, MorphDrawable.CORNER_RADIUS,
                endCornerRadius);

        AnimatorSet transition = new AnimatorSet();
        transition.playTogether(
                changeBounds,
                corners,
                color
        );
        transition.setDuration(200);
        transition.setInterpolator(interpolator);
        return transition;
    }
}
