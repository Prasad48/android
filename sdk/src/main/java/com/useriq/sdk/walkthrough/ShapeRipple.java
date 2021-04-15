package com.useriq.sdk.walkthrough;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.useriq.sdk.models.BaseShape;
import com.useriq.sdk.models.Circle;
import com.useriq.sdk.models.LifeCycleManager;
import com.useriq.sdk.models.ShapeRippleEntry;
import com.useriq.sdk.models.WTTheme;
import com.useriq.sdk.util.ScreenBoundsUtil;
import com.useriq.sdk.util.ShapePulseUtil;
import com.useriq.sdk.util.UnitUtil;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class ShapeRipple extends View {

    static final String TAG = ShapeRipple.class.getSimpleName();

    private static final int NO_VALUE = 0;

    /**
     * Default color of the ripple
     */
    private static final int DEFAULT_RIPPLE_COLOR = Color.parseColor("#FFF44336");

    /**
     * Default color of the start ripple color transition
     */
    private static final int DEFAULT_RIPPLE_FROM_COLOR = Color.parseColor("#FFF44336");

    /**
     * Default color of the end ripple color transition
     */
    private static final int DEFAULT_RIPPLE_TO_COLOR = Color.parseColor("#00FFFFFF");

    /**
     * The default duration of the ripples
     */
    private static final int DEFAULT_RIPPLE_DURATION = 2500;

    /**
     * The default ripple interval factor see {@link #rippleIntervalFactor} for
     * more details
     */
    private static final float DEFAULT_RIPPLE_INTERVAL_FACTOR = 1F;

    /**
     * Base ripple color, only used when {@link #enableColorTransition} flag is set to false
     */
    private int rippleColor;

    /**
     * Starting color for the color transition of the ripple, only
     * used when {@link #enableColorTransition} flag is set to true
     */
    private int rippleFromColor;

    /**
     * End color for the color transition of the ripple, only
     * used when {@link #enableColorTransition} flag is set to true
     */
    private int rippleToColor;

    /**
     * Base ripple duration for the animation, by default the value is {@value DEFAULT_RIPPLE_DURATION}
     */
    private int rippleDuration;

    /**
     * Base stroke width for each of the ripple
     */
    private int rippleStrokeWidth;

    /**
     * Ripple interval handles the actual timing of each spacing
     * of ripples in the list, calculated in {@link #onMeasure(int, int)}
     */
    private float rippleInterval;

    /**
     * Ripple maximum radius that will be used instead of the pre-calculated value, default value is
     * the size of the layout.
     */
    private float rippleMaximumRadius;

    /**
     * Ripple interval factor is the spacing for each ripple
     * the more the factor the more the spacing
     */
    private float rippleIntervalFactor;

    /**
     * Ripple count that will be rendered in the layout, default value is calculated based on the
     * layout_width / ripple_width
     */
    private int rippleCount = 2;

    /**
     * The width of the view in the layout which is calculated in {@link #onMeasure(int, int)}
     */
    private int viewWidth;

    /**
     * The height of the view in the layout which is calculated in {@link #onMeasure(int, int)}
     */
    private int viewHeight;

    /**
     * The maximum radius of the ripple which is calculated in the {@link #onMeasure(int, int)}
     */
    private int maxRippleRadius;

    /**
     * The last multiplier value of the animation after invalidation of this view
     */
    private float lastMultiplierValue = 0f;

    /**
     * Enables the color transition for each ripple, it is true by default
     */
    private boolean enableColorTransition = true;

    /**
     * Enables the single ripple, it is false by default
     */
    private boolean enableSingleRipple = false;

    /**
     * Enables the random positioning of the ripple, it is false by default
     */
    private boolean enableRandomPosition = false;

    /**
     * Enable the random color of the ripple, it is false by default
     */
    private boolean enableRandomColor = false;

    /**
     * Enables the stroke style of the ripples, it is false by default
     *
     * This means that if it is enabled it will use the {@link Paint#setStyle(Paint.Style)} as
     * {@link Paint.Style#STROKE}, by default it will use the {@link Paint.Style#FILL}.
     *
     */
    private boolean enableStrokeStyle = false;

    /**
     * The list of {@link ShapeRippleEntry} which is rendered in {@link #render(Float)}
     */
    private Deque<ShapeRippleEntry> shapeRippleEntries;

    /**
     * The list of developer predefined random colors which is used when {@link #enableRandomColor} is set to true.
     * <p>
     * If this is not defined by the developer it will have a default value from {@link ShapePulseUtil#generateRandomColours(Context)}
     */
    private List<Integer> rippleRandomColors;

    /**
     * The actual animator for the ripples, used in {@link #render(Float)}
     */
    private ValueAnimator rippleValueAnimator;

    /**
     * The {@link Interpolator} of the {@link #rippleValueAnimator}, by default it is {@link LinearInterpolator}
     */
    private Interpolator rippleInterpolator;

    /**
     * The random generator object for both color ({@link #enableRandomColor} is set to true) and position ({@link #enableRandomPosition} is set to true)
     */
    private Random random;

    /**
     * The renderer of shape ripples which is drawn in the {@link BaseShape#onDraw(Canvas, int, int, float, int, int, Paint)}
     */
    private BaseShape rippleShape;

    /**
     * The default paint for the ripple
     */
    protected Paint shapePaint;

    /**
     * This flag will handle that it was stopped by the user
     */
    private boolean isStopped;

    private int x,y;
    private int statusBarHeight;

    public ShapeRipple(Context context, WTTheme theme, int[] targetRect, View target) {
        super(context);
        statusBarHeight = ScreenBoundsUtil.getStatusHeight(context);
        calculateLocation(theme, targetRect, target);
        init(context);
    }

    void calculateLocation(WTTheme theme, int[] targetRect, View target) {
        if (theme.placement.type == WTStepView.WTPlacementType.MANUAL) {
            switch (theme.placement.location) {
                case WTStepView.WTLocation.TOP_LEFT: {
                    x = targetRect[0];
                    y = targetRect[1];
                    break;
                }
                case WTStepView.WTLocation.TOP_CENTER: {
                    x = targetRect[0] + target.getWidth() / 2;
                    y = targetRect[1];
                    break;
                }
                case WTStepView.WTLocation.TOP_RIGHT: {
                    x = targetRect[0] + target.getWidth();
                    y = targetRect[1];
                    break;
                }
                case WTStepView.WTLocation.CENTER_LEFT: {
                    x = targetRect[0];
                    y = targetRect[1] + target.getHeight() / 2;
                    break;
                }
                case WTStepView.WTLocation.CENTER_CENTER: {
                    x = targetRect[0] + target.getWidth() / 2;
                    y = targetRect[1] + target.getHeight() / 2;
                    break;
                }
                case WTStepView.WTLocation.CENTER_RIGHT: {
                    x = targetRect[0] + target.getWidth();
                    y = targetRect[1] + target.getHeight() / 2;
                    break;
                }
                case WTStepView.WTLocation.BOTTOM_LEFT: {
                    x = targetRect[0];
                    y = targetRect[1] + target.getHeight();
                    break;
                }
                case WTStepView.WTLocation.BOTTOM_CENTER: {
                    x = targetRect[0] + target.getWidth() / 2;
                    y = targetRect[1] + target.getHeight();
                    break;
                }
                case WTStepView.WTLocation.BOTTOM_RIGHT: {
                    x = targetRect[0] + target.getWidth();
                    y = targetRect[1] + target.getHeight();
                    break;
                }
            }
        } else {
            Rect screenBounds = ScreenBoundsUtil.getScreenBoundWithoutNav(target.getContext());
            int availableSpaceInBottom = screenBounds.bottom - (targetRect[1] + target.getHeight());
            int availableSpaceInTop = targetRect[1] - screenBounds.top;
            int availableSpaceInLeft = targetRect[0] - screenBounds.left;
            int availableSpaceInRight = screenBounds.right - (targetRect[0] + target.getWidth());

            if (availableSpaceInBottom >= 100) {
                x = targetRect[0] + target.getWidth() / 2;
                y = targetRect[1] + target.getHeight();
            } else if (availableSpaceInTop >= 100) {
                x = targetRect[0] + target.getWidth() / 2;
                y = targetRect[1];
            } else if (availableSpaceInLeft >= 100) {
                x = targetRect[0];
                y = targetRect[1] + target.getHeight() / 2;
            } else if (availableSpaceInRight >= 100) {
                x = targetRect[0] + target.getWidth();
                y = targetRect[1] + target.getHeight() / 2;
            } else {
                x = targetRect[0] + target.getWidth() / 2;
                y = targetRect[1] + target.getHeight() / 2;
            }
        }
        x += theme.placement.offset.get(0);
        y += theme.placement.offset.get(1) - statusBarHeight;
    }

    private void init(Context context) {

        // initialize the paint for the shape ripple
        shapePaint = new Paint();
        shapePaint.setAntiAlias(true);
        shapePaint.setDither(true);
        shapePaint.setStyle(Paint.Style.FILL);

        this.shapeRippleEntries = new LinkedList<>();
        this.random = new Random();

        rippleShape = new Circle();
        rippleShape.onSetup(context, shapePaint);

        rippleColor = DEFAULT_RIPPLE_COLOR;
        rippleFromColor = DEFAULT_RIPPLE_FROM_COLOR;
        rippleToColor = DEFAULT_RIPPLE_TO_COLOR;
        rippleStrokeWidth = UnitUtil.dpToPx(15);
        rippleRandomColors = ShapePulseUtil.generateRandomColours(getContext());
        rippleDuration = DEFAULT_RIPPLE_DURATION;
        rippleIntervalFactor = DEFAULT_RIPPLE_INTERVAL_FACTOR;

        rippleInterpolator = new LinearInterpolator();

        start(rippleDuration);

        // Only attach the activity for ICE_CREAM_SANDWICH and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            /**
             * The life activity life cycle the shape ripple uses.
             */
            LifeCycleManager lifeCycleManager = new LifeCycleManager(this);
            lifeCycleManager.attachListener();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (ShapeRippleEntry shapeRippleEntry : shapeRippleEntries) {

            if (shapeRippleEntry.isRender()) {
                // Each ripple entry is a rendered as a shape
                shapeRippleEntry.getBaseShape().onDraw(canvas, shapeRippleEntry.getX(),
                        shapeRippleEntry.getY(),
                        shapeRippleEntry.getRadiusSize(),
                        shapeRippleEntry.getChangingColorValue(),
                        shapeRippleEntry.getRippleIndex(),
                        shapePaint);
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Get the measure base of the measure spec
        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        initializeEntries(rippleShape);

        rippleShape.setWidth(viewWidth);
        rippleShape.setHeight(viewHeight);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        stop();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        stop();
    }

    /**
     * This method will initialize the list of {@link ShapeRippleEntry} with
     * initial position, color, index, and multiplier value.)
     *
     * @param shapeRipple the renderer of shape ripples
     */
    private void initializeEntries(BaseShape shapeRipple) {
        // Sets the stroke width of the ripple
        shapePaint.setStrokeWidth(rippleStrokeWidth);

        if (viewWidth == 0 && viewHeight == 0) {
            return;
        }

        // we remove all the shape ripples entries
        shapeRippleEntries.clear();

        // the ripple radius based on the x or y
        maxRippleRadius = rippleMaximumRadius != NO_VALUE ? (int)rippleMaximumRadius :
                (Math.min(viewWidth, viewHeight) / 2 - (rippleStrokeWidth / 2));

        // Calculate the max number of ripples
        rippleCount = rippleCount > NO_VALUE ? rippleCount : maxRippleRadius / rippleStrokeWidth;

        // Calculate the interval of ripples
        rippleInterval = DEFAULT_RIPPLE_INTERVAL_FACTOR / rippleCount;

        for (int i = 0; i < rippleCount; i++) {
            ShapeRippleEntry shapeRippleEntry = new ShapeRippleEntry(shapeRipple);
            shapeRippleEntry.setX(enableRandomPosition ? random.nextInt(viewWidth) : x);
            shapeRippleEntry.setY(enableRandomPosition ? random.nextInt(viewHeight) : y);
            shapeRippleEntry.setMultiplierValue(-(rippleInterval * (float) i));
            shapeRippleEntry.setRippleIndex(i);

            if (enableRandomColor) {
                shapeRippleEntry.setOriginalColorValue(rippleRandomColors.get(random.nextInt(rippleRandomColors.size())));
            } else {
                shapeRippleEntry.setOriginalColorValue(rippleColor);
            }

            shapeRippleEntries.add(shapeRippleEntry);

            // we only render 1 ripple when it is enabled
            if (enableSingleRipple) {
                break;
            }
        }
    }

    /**
     * Refreshes the list of ticket entries after certain options are changed such as the {@link #rippleColor},
     * {@link #rippleShape}, {@link #enableRandomPosition}, etc.
     * <p>
     * This will only execute after the {@link #initializeEntries(BaseShape)}, this is safe to call before it.
     */
    private void reconfigureEntries() {

        // we do not re configure when dimension is not calculated
        // or if the list is empty
        if (viewWidth == 0 && viewHeight == 0 && (shapeRippleEntries == null || shapeRippleEntries.size() == 0)) {
            return;
        }

        // sets the stroke width of the ripple
        shapePaint.setStrokeWidth(rippleStrokeWidth);

        for (ShapeRippleEntry shapeRippleEntry : shapeRippleEntries) {
            if (enableRandomColor) {
                shapeRippleEntry.setOriginalColorValue(rippleRandomColors.get(random.nextInt(rippleRandomColors.size())));
            } else {
                shapeRippleEntry.setOriginalColorValue(rippleColor);
            }

            shapeRippleEntry.setBaseShape(rippleShape);
        }
    }

    /**
     * Start the {@link #rippleValueAnimator} with specified duration for each ripple.
     *
     * @param millis the duration in milliseconds
     */
    void start(int millis) {

        // Do a ripple value renderer
        rippleValueAnimator = ValueAnimator.ofFloat(0f, 1f);
        rippleValueAnimator.setDuration(millis);
        rippleValueAnimator.setRepeatMode(ValueAnimator.RESTART);
        rippleValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rippleValueAnimator.setInterpolator(rippleInterpolator);
        rippleValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                render((Float) animation.getAnimatedValue());
            }
        });

        rippleValueAnimator.start();
    }

    /**
     * This is the main renderer for the list of ripple, we always check that the first ripple is already
     * finished.
     * <p>
     * When the ripple is finished it is {@link ShapeRippleEntry#reset()} and move to the end of the list to be reused all over again
     * to prevent creating a new instance of it.
     * <p>
     * Each ripple will be configured to be either rendered or not rendered to the view to prevent extra rendering process.
     *
     * @param multiplierValue the current multiplier value of the {@link #rippleValueAnimator}
     */
    private void render(Float multiplierValue) {

        // Do not render when entries are empty
        if (shapeRippleEntries.size() == 0) {
            return;
        }

        ShapeRippleEntry firstEntry = shapeRippleEntries.peekFirst();

        // Calculate the multiplier value of the first entry
        float firstEntryMultiplierValue = firstEntry.getMultiplierValue() + Math.max(multiplierValue - lastMultiplierValue, 0);

        // Check if the first entry is done the ripple (happens when the ripple reaches to end)
        if (firstEntryMultiplierValue >= 1.0f) {

            // Remove and relocate the first entry to the last entry
            ShapeRippleEntry removedEntry = shapeRippleEntries.pop();
            removedEntry.reset();
            removedEntry.setOriginalColorValue(enableRandomColor ? rippleRandomColors.get(random.nextInt(rippleRandomColors.size())) : rippleColor);
            shapeRippleEntries.addLast(removedEntry);

            // Get the new first entry of the list
            firstEntry = shapeRippleEntries.peekFirst();

            // Calculate the new multiplier value of the first entry of the list
            firstEntryMultiplierValue = firstEntry.getMultiplierValue() + Math.max(multiplierValue - lastMultiplierValue, 0);

            firstEntry.setX(enableRandomPosition ? random.nextInt(viewWidth) : x);
            firstEntry.setY(enableRandomPosition ? random.nextInt(viewHeight) : y);

            if (enableSingleRipple) {
                firstEntryMultiplierValue = 0;
            }
        }

        int index = 0;
        for (ShapeRippleEntry shapeRippleEntry : shapeRippleEntries) {

            // set the updated index
            shapeRippleEntry.setRippleIndex(index);

            // calculate the shape multiplier by index
            float currentEntryMultiplier = firstEntryMultiplierValue - rippleInterval * index;

            // Check if we render the current ripple in the list
            // We render when the multiplier value is >= 0
            if (currentEntryMultiplier >= 0) {
                shapeRippleEntry.setRender(true);
            } else {
                // We continue to the next item
                // since we know that we do not
                // need the calculations below
                shapeRippleEntry.setRender(false);
                continue;
            }

            // We already calculated the multiplier value of the first entry of the list
            if (index == 0) {
                shapeRippleEntry.setMultiplierValue(firstEntryMultiplierValue);
            } else {
                shapeRippleEntry.setMultiplierValue(currentEntryMultiplier);
            }

            // calculate the color if we enabled the color transition
            shapeRippleEntry.setChangingColorValue(enableColorTransition
                    ? ShapePulseUtil.evaluateTransitionColor(currentEntryMultiplier, shapeRippleEntry.getOriginalColorValue(), rippleToColor)
                    : rippleColor);

            // calculate the current ripple size
            shapeRippleEntry.setRadiusSize(maxRippleRadius * currentEntryMultiplier);

            index += 1;
        }

        // save the last multiplier value
        lastMultiplierValue = multiplierValue;

        // we draw the shapes
        invalidate();
    }

    /**
     * Stop the {@link #rippleValueAnimator} and clears the {@link #shapeRippleEntries}
     */
    public void stop() {

        if (rippleValueAnimator != null) {
            rippleValueAnimator.cancel();
            rippleValueAnimator.end();
            rippleValueAnimator.removeAllUpdateListeners();
            rippleValueAnimator.removeAllListeners();
            rippleValueAnimator = null;
        }

        if (shapeRippleEntries != null) {
            shapeRippleEntries.clear();
            invalidate();
        }
    }

    /**
     * Starts the ripple by stopping the current {@link #rippleValueAnimator} using the {@link #stop()}
     * then initializing ticket entries using the {@link #initializeEntries(BaseShape)}
     * and lastly starting the {@link #rippleValueAnimator} using {@link #start(int)}
     */
    public void startRipple() {
        //stop the animation from previous before starting it again
        stop();
        initializeEntries(rippleShape);
        start(rippleDuration);

        this.isStopped = false;
    }

    /**
     * Stops the ripple see {@link #stop()} for more details
     */
    public void stopRipple() {
        stop();

        this.isStopped = true;
    }

    /**
     * This restarts the ripple or continue where it was left off, this is mostly used
     * for {@link LifeCycleManager}.
     */
    public void restartRipple() {
        if (this.isStopped) {
            return;
        }

        startRipple();
    }

    /**
     * @return The max ripple radius
     */
    public float getRippleMaximumRadius() {
        return maxRippleRadius;
    }

    /**
     * @return True if color transition is enabled
     */
    public boolean isEnableColorTransition() {
        return enableColorTransition;
    }

    /**
     * @return True of single ripple is enabled
     */
    public boolean isEnableSingleRipple() {
        return enableSingleRipple;
    }

    /**
     * @return True of random ripple position is enabled
     */
    public boolean isEnableRandomPosition() {
        return enableRandomPosition;
    }

    /**
     * @return The stroke width(in pixels) for each ripple
     */
    public int getRippleStrokeWidth() {
        return rippleStrokeWidth;
    }

    /**
     * @return The base ripple color
     */
    public int getRippleColor() {
        return rippleColor;
    }

    /**
     * @return The starting ripple color of the color transition
     */
    public int getRippleFromColor() {
        return rippleFromColor;
    }

    /**
     * @return The end ripple color of the color transition
     */
    public int getRippleToColor() {
        return rippleToColor;
    }

    /**
     * @return The duration of each ripple in milliseconds
     */
    public int getRippleDuration() {
        return rippleDuration;
    }

    /**
     * @return The number of ripple being rendered
     */
    public int getRippleCount() {
        return rippleCount;
    }
    /**
     * @return The interpolator of the value animator
     */
    public Interpolator getRippleInterpolator() {
        return rippleInterpolator;
    }

    /**
     * @return True if random color for each ripple is enabled
     */
    public boolean isEnableRandomColor() {
        return enableRandomColor;
    }

    /**
     * @return True if it is using STROKE style for each ripple
     */
    public boolean isEnableStrokeStyle() {
        return enableStrokeStyle;
    }

    /**
     * @return The shape renderer for the shape ripples
     */
    public BaseShape getRippleShape() {
        return rippleShape;
    }

    /**
     * @return The list of developer predefined random colors
     */
    public List<Integer> getRippleRandomColors() {
        return rippleRandomColors;
    }

    /**
     * Change the maximum size of the ripple, default to the size of the layout.
     * <p>
     * Value must be greater than 1
     *
     * @param rippleMaximumRadius The floating ripple interval for each ripple
     */
    public void setRippleMaximumRadius(float rippleMaximumRadius) {
        if (rippleMaximumRadius <= NO_VALUE) {
            throw new IllegalArgumentException("Ripple max radius must be greater than 0");
        }

        this.rippleMaximumRadius = rippleMaximumRadius;
        requestLayout();
    }

    /**
     * Enables the color transition for each ripple
     *
     * @param enableColorTransition flag for enabling color trasition
     */
    public void setEnableColorTransition(boolean enableColorTransition) {
        this.enableColorTransition = enableColorTransition;
    }

    /**
     * Enables the single ripple rendering
     *
     * @param enableSingleRipple flag for enabling single ripple
     */
    public void setEnableSingleRipple(boolean enableSingleRipple) {
        this.enableSingleRipple = enableSingleRipple;

        initializeEntries(rippleShape);
    }

    /**
     * Change the stroke width for each ripple
     *
     * @param rippleStrokeWidth The stroke width in pixel
     */
    public void setRippleStrokeWidth(int rippleStrokeWidth) {

        if (rippleStrokeWidth <= 0) {
            throw new IllegalArgumentException("Ripple duration must be > 0");
        }

        this.rippleStrokeWidth = rippleStrokeWidth;
    }

    /**
     * Change the base color of each ripple
     *
     * @param rippleColor The ripple color
     */
    public void setRippleColor(int rippleColor) {
        setRippleColor(rippleColor, true);
    }

    /**
     * Change the base color of each ripple
     *
     * @param rippleColor The ripple color
     * @param instant     flag for when changing color is instant without delay
     */
    public void setRippleColor(int rippleColor, boolean instant) {
        this.rippleColor = rippleColor;

        if (instant) {
            reconfigureEntries();
        }
    }

    /**
     * Change the starting color of the color transition
     *
     * @param rippleFromColor The starting color
     */
    public void setRippleFromColor(int rippleFromColor) {
        setRippleFromColor(rippleFromColor, true);
    }

    /**
     * Change the starting color of the color transition
     *
     * @param rippleFromColor The starting color
     * @param instant         flag for when changing color is instant without delay
     */
    public void setRippleFromColor(int rippleFromColor, boolean instant) {
        this.rippleFromColor = rippleFromColor;

        if (instant) {
            reconfigureEntries();
        }
    }

    /**
     * Change the end color of the color transition
     *
     * @param rippleToColor The end color
     */
    public void setRippleToColor(int rippleToColor) {
        setRippleToColor(rippleToColor, true);
    }

    /**
     * Change the end color of the color transition
     *
     * @param rippleToColor The end color
     * @param instant       flag for when changing color is instant without delay
     */
    public void setRippleToColor(int rippleToColor, boolean instant) {
        this.rippleToColor = rippleToColor;

        if (instant) {
            reconfigureEntries();
        }
    }

    /**
     * Change the ripple duration of the animator
     *
     * @param millis The duration in milliseconds
     */
    public void setRippleDuration(int millis) {

        if (rippleDuration <= 0) {
            throw new IllegalArgumentException("Ripple duration must be > 0");
        }

        this.rippleDuration = millis;

        // We set the duration here this will auto change the animator
        if (rippleValueAnimator != null) {
            rippleValueAnimator.setDuration(rippleDuration);
        }
    }

    /**
     * Enables the random positioning of ripples
     *
     * @param enableRandomPosition flag for enabling random position
     */
    public void setEnableRandomPosition(boolean enableRandomPosition) {
        this.enableRandomPosition = enableRandomPosition;

        initializeEntries(rippleShape);
    }

    /**
     * Change the {@link Interpolator} of the animator
     *
     * @param rippleInterpolator The interpolator
     */
    public void setRippleInterpolator(Interpolator rippleInterpolator) {

        if (rippleInterpolator == null) {
            throw new NullPointerException("Ripple interpolator in null");
        }

        this.rippleInterpolator = rippleInterpolator;
    }

    /**
     * Enables the random coloring of each ripple
     *
     * @param enableRandomColor flag for enabling random color
     */
    public void setEnableRandomColor(boolean enableRandomColor) {
        this.enableRandomColor = enableRandomColor;

        reconfigureEntries();
    }

    /**
     * Change the number of ripples, default value is calculated based on the
     * layout_width / ripple_width.
     *
     * @param rippleCount The number of ripples
     */
    public void setRippleCount(int rippleCount) {
        if (rippleCount <= NO_VALUE) {
            throw new NullPointerException("Invalid ripple count");
        }

        this.rippleCount = rippleCount;
        requestLayout();
    }

    /**
     * Enables the stroke style of each ripple
     *
     * @param enableStrokeStyle flag for enabling STROKE style
     */
    public void setEnableStrokeStyle(boolean enableStrokeStyle) {
        this.enableStrokeStyle = enableStrokeStyle;

        if (enableStrokeStyle) {
            this.shapePaint.setStyle(Paint.Style.STROKE);
        } else {
            this.shapePaint.setStyle(Paint.Style.FILL);
        }
    }

    /**
     * Change the shape renderer of the ripples
     *
     * @param rippleShape The renderer of shapes ripple
     */
    public void setRippleShape(BaseShape rippleShape) {
        this.rippleShape = rippleShape;

        // Make sure we call onSetup right away
        this.rippleShape.onSetup(getContext(), this.shapePaint);

        reconfigureEntries();
    }

    /**
     * Change the developer predefined random colors
     *
     * @param rippleRandomColors The list of colors
     */
    public void setRippleRandomColors(List<Integer> rippleRandomColors) {

        if (rippleRandomColors == null) {
            throw new NullPointerException("List of colors cannot be null");
        }

        if (rippleRandomColors.size() == 0) {
            throw new IllegalArgumentException("List of color cannot be empty");
        }

        // We clear the list of colors before adding new colors
        this.rippleRandomColors.clear();

        this.rippleRandomColors = rippleRandomColors;

        reconfigureEntries();
    }

}

