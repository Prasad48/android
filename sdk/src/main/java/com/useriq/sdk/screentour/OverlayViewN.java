package com.useriq.sdk.screentour;

import android.app.Activity;
import android.app.Application;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.useriq.sdk.MatchFinder;
import com.useriq.sdk.R;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.models.STStep;
import com.useriq.sdk.models.ScreenTour;
import com.useriq.sdk.util.PathUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class OverlayViewN extends ShowcaseView {
    private final static int DOWN_ARROW = 0;
    private final static int UP_ARROW = 1;
    private final static int NONE = 2;
    private final Application mApp;
    private final int mArrowHeight;
    private final int mArrowHeadHeight;
    private final Paint mPaint;
    private final int mArrowBend;
    private final Path mPath;
    private final PointF mP2;
    private final PointF mP1;
    private final Rect mRect;
    private final ArrayList<Rect> mViewBounds;
    private final ArrayList<Integer> mArrowsDirections;
    String mId;
    private boolean mChildAdded;


    private OverlayViewN(Application context, ArrayList<Element> elements, int paddingFromTarget, @ColorInt int bgColor, @ColorInt int borderColor, @ColorInt int textColor, int textSize, String id) {
        super(context, elements, paddingFromTarget, bgColor, borderColor, true);
        mArrowBend = UserIQSDKInternal.getInstance().getResources().getDimensionPixelSize(R.dimen.appunfold_arrow_bend);
        mArrowHeight = UserIQSDKInternal.getInstance().getResources().getDimensionPixelSize(R.dimen.appunfold_arrow_height);
        mArrowHeadHeight = UserIQSDKInternal.getInstance().getResources().getDimensionPixelSize(R.dimen.appunfold_arrow_head_height);
        mArrowsDirections = new ArrayList<>();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPath = new Path();
        mP1 = new PointF();
        mP2 = new PointF();
        mViewBounds = new ArrayList<>();
        mRect = new Rect();
        mId = id;
        mApp = context;
        for (int i = 0; i < elements.size(); i++) {
            TextView tvElement = getTextView(elements.get(i).desc);
            tvElement.setTextColor(textColor);
            //   textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, context.getResources().getDisplayMetrics());
            //textSize = UnitUtil.dpToPx(context, textSize);
            if (textSize != 0) {
                tvElement.setTextSize(textSize);
            }
            addView(tvElement);
        }

        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mChildAdded = true;
    }

    public static OverlayViewN show(@NonNull Activity activity, final ScreenTour st) {
        ArrayList<STStep> steps = st.steps;
        final ArrayList<ShowcaseView.Element> elements = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            STStep step = steps.get(i);
            View targetView = MatchFinder.findView(activity, step.element);
            Rect inset = PathUtil.getPositionForView(targetView);
            if (inset == null) continue; // ignore missing views)
            elements.add(new ShowcaseView.Element(ShowcaseView.Element.RECT, inset, step.desc));
        }
        if (elements.size() == 0) {
            Toast.makeText(activity, "View not found", Toast.LENGTH_SHORT).show();
            return null;
        }
     /*   final OverlayViewN overlayViewN = new OverlayViewN(activity, elements, UserIQSDKInternal.getInstance().getResources().getDimensionPixelOffset(R.dimen.target_padding),
                Color.parseColor("#CC000011"));*/
        final OverlayViewN overlayViewN;
        if (st.theme != null) {
            overlayViewN = new OverlayViewN(activity.getApplication(), elements, UserIQSDKInternal.getInstance().getResources().getDimensionPixelOffset(R.dimen.appunfold_target_padding),
                    st.theme.bgColor, st.theme.borderColor, st.theme.textColor, st.theme.textSize, st.id);
        } else {
            overlayViewN = new OverlayViewN(activity.getApplication(), elements, UserIQSDKInternal.getInstance().getResources().getDimensionPixelOffset(R.dimen.appunfold_target_padding),
                    Color.parseColor("#CC000011"), Color.TRANSPARENT, Color.WHITE, 0, st.id);
        }

        overlayViewN.setVisibility(VISIBLE);
        overlayViewN.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                overlayViewN.stopOverlay(st);
            }
        });
//        UIManager.getInstance().addViewToUxLayout(overlayViewN, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        UserIQSDKInternal.getAnalyticsManager().onTour(st.id,st.steps.get(0).id, true);
        return overlayViewN;
    }

    public void stopOverlay(ScreenTour st) {
//        UIManager.getInstance().setFab();
//        UIManager.getInstance().removeViewFromUxLayout(this);
        UserIQSDKInternal.getAnalyticsManager().onTour(st.id,st.steps.get(st.steps.size() - 1).id, false);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mChildAdded) {
            int count = getChildCount();
            @ArrowDirection int dir = NONE;
            for (int i = 0; i < count; i++) {
                Rect elementBounds = mElements.get(i).bounds;
                View view = getChildAt(i);
                int totalHeightRequired = view.getMeasuredHeight() + mArrowHeight;
                boolean upArrowPossible = false, downArrowPossible = false;
                if (elementBounds.top - getTop() > totalHeightRequired) {
                    downArrowPossible = true;
                }
                if (getBottom() - elementBounds.bottom > totalHeightRequired) {
                    upArrowPossible = true;
                }

                for (int j = 0; j < mViewBounds.size() && (upArrowPossible || downArrowPossible); j++) {
                    if (upArrowPossible && mViewBounds.get(j).top - elementBounds.bottom < totalHeightRequired) {
                        upArrowPossible = false;
                    }
                    if (downArrowPossible && elementBounds.top - mViewBounds.get(j).bottom < totalHeightRequired) {
                        downArrowPossible = false;
                    }
                }

                if (downArrowPossible) {
                    mArrowsDirections.add(DOWN_ARROW);
                } else if (upArrowPossible) {
                    mArrowsDirections.add(UP_ARROW);
                } else {
                    mArrowsDirections.add(NONE);
                }
                boolean rightAligned = false, leftAligned = false, centerAligned = false;
                if (upArrowPossible || downArrowPossible) {
                    if (getRight() - elementBounds.centerX() < view.getMeasuredWidth() / 2) {
                        rightAligned = true;
                    }
                    if (elementBounds.centerX() - getLeft() < view.getMeasuredWidth() / 2) {
                        leftAligned = true;
                    }
                    if (!rightAligned && !leftAligned) {
                        centerAligned = true;
                    }
                    if (downArrowPossible) {
                        if (rightAligned) {
                            view.layout(getRight() - view.getMeasuredWidth(),
                                    elementBounds.top - mArrowHeight - view.getMeasuredHeight(),
                                    getRight(),
                                    elementBounds.top - mArrowHeight
                            );
                        } else if (leftAligned) {
                            view.layout(getLeft(),
                                    elementBounds.top - mArrowHeight - view.getMeasuredHeight(),
                                    getLeft() + view.getMeasuredWidth(),
                                    elementBounds.top - mArrowHeight
                            );
                        } else if (centerAligned) {
                            view.layout(elementBounds.centerX() - view.getMeasuredWidth() / 2,
                                    elementBounds.top - mArrowHeight - view.getMeasuredHeight(),
                                    elementBounds.centerX() + view.getMeasuredWidth() / 2,
                                    elementBounds.top - mArrowHeight
                            );
                        }
                        mViewBounds.add(new Rect(elementBounds.left, elementBounds.top - mArrowHeight, elementBounds.right,
                                elementBounds.bottom + view.getMeasuredHeight()));

                    } else if (upArrowPossible) {
                        if (rightAligned) {
                            view.layout(getRight() - view.getMeasuredWidth(),
                                    elementBounds.bottom + mArrowHeight,
                                    getRight(),
                                    elementBounds.bottom + mArrowHeight + view.getMeasuredHeight()
                            );

                        } else if (leftAligned) {
                            view.layout(getLeft(),
                                    elementBounds.bottom + mArrowHeight,
                                    getLeft() + view.getMeasuredWidth(),
                                    elementBounds.bottom + mArrowHeight + view.getMeasuredHeight()
                            );
                        } else if (centerAligned) {
                            view.layout(elementBounds.centerX() - view.getMeasuredWidth() / 2,
                                    elementBounds.bottom + mArrowHeight,
                                    elementBounds.centerX() + view.getMeasuredWidth() / 2,
                                    elementBounds.bottom + mArrowHeight + view.getMeasuredHeight()
                            );
                        }
                        mViewBounds.add(new Rect(elementBounds.left, elementBounds.top, elementBounds.right,
                                elementBounds.bottom + mArrowHeight + view.getMeasuredHeight()));

                    }

                } else {
                    view.setVisibility(View.GONE);
                }

            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mElements.size(); i++) {
            if (mArrowsDirections.get(i) == DOWN_ARROW) {
                drawDownArrow(mElements.get(i).bounds);
            } else if (mArrowsDirections.get(i) == UP_ARROW) {
                drawUpArrow(mElements.get(i).bounds);
            }
            canvas.drawPath(mPath, mPaint);

        }


    }

    int heightRequiredForView(String text) {
        TextView textView = getTextView(text);
        textView.measure(MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.AT_MOST));
        return textView.getMeasuredHeight() + mArrowHeight;
    }

    TextView getTextView(String text) {
        TextView textView = new TextView(mApp);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        LayoutParams lp = new LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lp.gravity = Gravity.TOP;
        textView.setLayoutParams(lp);
        return textView;
    }

    private void drawUpArrow(Rect elementBounds) {
        mP1.set(elementBounds.centerX(), elementBounds.bottom + mArrowHeight);
        mP2.set(elementBounds.centerX(), elementBounds.bottom);
        mPath.reset();
        mPath.moveTo(mP1.x, mP1.y);
        mPath.quadTo(mP1.x - mArrowBend, mP1.y - (mArrowHeight * 0.4f), mP2.x, mP2.y);
        mPath.quadTo(
                mP2.x - (mArrowHeadHeight * 0.2f),
                mP2.y + (mArrowHeadHeight * 0.4f),
                mP2.x - (mArrowHeadHeight * 0.6f),
                mP2.y + mArrowHeadHeight
        );
        mPath.moveTo(mP2.x, mP2.y);
        mPath.quadTo(
                mP2.x,
                mP2.y + (mArrowHeadHeight * 0.4f),
                mP2.x + (mArrowHeadHeight * 0.2f),
                mP2.y + mArrowHeadHeight
        );
    }

    private void drawDownArrow(Rect elementBounds) {
        mP1.set(elementBounds.centerX(), elementBounds.top - mArrowHeight);
        mP2.set(elementBounds.centerX(), elementBounds.top);
        mPath.reset();
        mPath.moveTo(mP1.x, mP1.y);
        mPath.quadTo(mP1.x + mArrowBend, mP1.y + (mArrowHeight * 0.4f), mP2.x, mP2.y);
        mPath.quadTo(
                mP2.x,
                mP2.y - (mArrowHeadHeight * 0.4f),
                mP2.x - (mArrowHeadHeight * 0.2f),
                mP2.y - mArrowHeadHeight
        );
        mPath.moveTo(mP2.x, mP2.y);
        mPath.quadTo(
                mP2.x + (mArrowHeadHeight * 0.2f),
                mP2.y - (mArrowHeadHeight * 0.4f),
                mP2.x + (mArrowHeadHeight * 0.6f),
                mP2.y - mArrowHeadHeight
        );
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DOWN_ARROW, UP_ARROW, NONE})
    public @interface ArrowDirection {
    }


}
