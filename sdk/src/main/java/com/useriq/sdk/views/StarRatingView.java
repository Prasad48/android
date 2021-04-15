package com.useriq.sdk.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.useriq.sdk.models.StarModel;
import com.useriq.sdk.util.UnitUtil;

import java.util.ArrayList;

/**
 * RatingStar is specific RatingBar use star drawable as the progress mark.
 * https://github.com/everhad/AndroidRatingStar
 * <p>
 * NOTE:
 * Padding will be larger if is {@link #cornerRadius} is set (No exact calc to handle this issue).
 */
public class StarRatingView extends View implements View.OnClickListener {

    private static final String TAG = "RatingStar";
    private static final int DEFAULT_STAR_HEIGHT = 12;
    private float cornerRadius = 4f;
    private int starForegroundColor;
    private int starBackgroundColor = Color.WHITE;
    private CornerPathEffect pathEffect;
    private ArrayList<StarModel> starList;
    private float rating = 0;
    private int borderColor;
    private int borderWidth;
    private int firstStarStartCor=0;
    private int starTopCor=0;
    /**
     * expected star number.
     */
    private int starNum = 5;
    /**
     * real drawn star number.
     */
    private int starCount = 5;
    private int starWidth = UnitUtil.dpToPx(35);
    private float starHeight = UnitUtil.dpToPx(35);
    private int starMargin = UnitUtil.dpToPx(15);
    private float clickedX, clickedY;
    private Paint paint;
    private MyOnClickListener mOuterOnClickListener;

    // region constructors
    public StarRatingView(Context context) {
        super(context);
        starForegroundColor = Color.argb(255, 227, 186, 39);
        borderColor = Color.argb(255, 227, 186, 39);
        borderWidth = 1;
        init();
    }

    public StarRatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        float strokeWidth = 2f;
        paint.setStrokeWidth(strokeWidth);

        // properties
        pathEffect = new CornerPathEffect(cornerRadius);

        // click to rate
        super.setOnClickListener(this);
    }

    public void setRating(float newRating) {
        if (rating != newRating) {
            rating = newRating;
            invalidate();
        }
    }

    public void setStarBackgroundColor(int starBackgroundColor) {
        this.starBackgroundColor = starBackgroundColor;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * How many stars to show, one star means one score = 1f. See {@link #setRating(float)}<br />
     * NOTE: The star's height is made by contentHeight by default.So, be sure to has defined the
     * correct StarView's height.
     *
     * @param count star count.
     */
    public void setStarNum(int count) {
        if (starNum != count) {
            starNum = count;
            calcStars();
            invalidate();
        }
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public void setStarForegroundColor(int starForegroundColor) {
        this.starForegroundColor = starForegroundColor;
        invalidate();
    }

    /**
     * Create all stars data, according to the contentWidth/contentHeight.
     */
    private void calcStars() {
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int contentWidth = getWidth() - paddingLeft - paddingRight;

        int left = (contentWidth - (starWidth*starNum) - starMargin*(starNum-1))/2;
        firstStarStartCor = left;
        int top = (int) ((getHeight() - starHeight)/2);
        starTopCor = top;

        Log.d(TAG, "drawing starCount = " + starCount + ", contentWidth = " + contentWidth
                + ", startWidth = " + starWidth + ", starHeight = " + starHeight);

        starList = new ArrayList<>(starCount);

        for (int i = 0; i < starCount; i++) {
            float starThicknessFactor = StarModel.DEFAULT_THICKNESS;
            StarModel star = new StarModel(starThicknessFactor);
            starList.add(star);
            star.setDrawingOuterRect(left, top, (int)starHeight);
            left += starWidth + 0.5f + starMargin;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float width;
        int height; // must have height

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = DEFAULT_STAR_HEIGHT;
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        float starHeight = height - getPaddingBottom() - getPaddingTop();

        if (widthMode == MeasureSpec.EXACTLY) {
            // Parent has told us how big to be. So be it.
            width = widthSize;
        } else {
            // get the perfect width
            width = getPaddingLeft() + getPaddingRight();
            if (starNum > 0) {
                if (starHeight > 0) {
                    width += starMargin * (starNum - 1);
                    width += StarModel.getStarWidth(starHeight) * starNum;
                }
            }

            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(widthSize, width);
            }
        }

        Log.d(TAG, "[onMeasure] width = " + width + ", pLeft = " + getPaddingLeft()
                + ", pRight = " + getPaddingRight() + ", starMargin = " + starMargin
                + ", starHeight = " + starHeight + ", starWidth = " + StarModel.getStarWidth(starHeight));

        int widthInt = (int) (width);
        if (widthInt < width) {
            widthInt++;
        }

        setMeasuredDimension(widthInt, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (starList == null) {
            calcStars();
        }

        if (starList == null || starList.size() == 0) {
            return;
        }

        for (int i = 0; i < starList.size(); i++) {
            if (rating >= i + 1) {
                drawFullStar(starList.get(i), canvas);
            } else {
                drawEmptyStar(starList.get(i), canvas);
            }
        }
    }

    private void drawFullStar(StarModel star, Canvas canvas) {
        drawSolidStar(star, canvas, starForegroundColor);
        drawStarStroke(star, canvas);
    }

    private void drawEmptyStar(StarModel star, Canvas canvas) {
        drawSolidStar(star, canvas, starBackgroundColor);
        drawStarStroke(star, canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h != oldh) {
            calcStars();
        }
    }

    private void drawSolidStar(StarModel star, Canvas canvas, int fillColor) {
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(fillColor);
        paint.setPathEffect(pathEffect);

        VertexF prev = star.getVertex(1);
        Path path = new Path();

        for (int i = 0; i < 5; i++) {
            path.rewind();
            path.moveTo(prev.x, prev.y);

            VertexF next = prev.next;

            path.lineTo(next.x, next.y);
            path.lineTo(next.next.x, next.next.y);
            path.lineTo(next.next.x, next.next.y);
            canvas.drawPath(path, paint);

            prev = next.next;
        }

        // fill the middle hole. use +1.0 +1.5 because the path-API will leave 1px gap.
        path.rewind();
        prev = star.getVertex(1);
        path.moveTo(prev.x - 1f, prev.y - 1f);
        prev = prev.next.next;
        path.lineTo(prev.x + 1.5f, prev.y - 0.5f);
        prev = prev.next.next;
        path.lineTo(prev.x + 1.5f, prev.y + 1f);
        prev = prev.next.next;
        path.lineTo(prev.x, prev.y + 1f);
        prev = prev.next.next;
        path.lineTo(prev.x - 1f, prev.y + 1f);

        paint.setPathEffect(null);
        canvas.drawPath(path, paint);
    }

    private void drawStarStroke(StarModel star, Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setColor(borderColor);
        paint.setPathEffect(pathEffect);
        VertexF prev = star.getVertex(1);
        Path path = new Path();

        for (int i = 0; i < 5; i++) {
            path.rewind();
            path.moveTo(prev.x, prev.y);

            VertexF next = prev.next;

            path.lineTo(next.x, next.y);
            path.lineTo(next.next.x, next.next.y);
            path.lineTo(next.next.x, next.next.y);

            canvas.drawPath(path, paint);
            prev = next.next;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            clickedX = event.getX();
            clickedY = event.getY();
        }
        return super.onTouchEvent(event);
    }

    public void setOnClickListener(MyOnClickListener onClickListener) {
        mOuterOnClickListener = onClickListener;
    }

    @Override
    public void onClick(View v) {
        changeRatingByClick();
    }

    private void changeRatingByClick() {
        if (clickedY < starTopCor || clickedY > starTopCor + starHeight) {
            return;
        }

        float starWidth = this.starWidth;
        float starMargin = this.starMargin;

        float left = firstStarStartCor;
        for (byte i = 1; i <= starCount; i++) {
            float right = left + starWidth;
            if (clickedX >= left && clickedX <= right) {
                setRating(i);
                if (mOuterOnClickListener != null) {
                    mOuterOnClickListener.onRatingChange(i);
                }
                break;
            }

            left += (starWidth + starMargin);
        }
    }

    public static class VertexF {
        public VertexF() {
        }

        public VertexF(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float x;
        public float y;

        public VertexF next;
    }

    public interface MyOnClickListener {
        void onRatingChange(int rating);
    }

}