package com.useriq.sdk.helpcenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.useriq.Logger;
import com.useriq.sdk.R;
import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.models.QDesc;
import com.useriq.sdk.models.Question;
import com.useriq.sdk.models.SyncData;
import com.useriq.sdk.models.WTStep;
import com.useriq.sdk.models.Walkthrough;
import com.useriq.sdk.screentour.ShowcaseView;
import com.useriq.sdk.walkthrough.ToolTipViewOld;
import com.useriq.sdk.walkthrough.WalkthroughImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.text.Html.FROM_HTML_MODE_COMPACT;
import static com.useriq.sdk.util.UnitUtil.dpToPx;

/**
 * @author sudhakar
 * @created 23-Oct-2018
 */
class AnswerAdapter extends BaseAdapter {
    private static final Logger logger = Logger.init(AnswerAdapter.class.getSimpleName());
    private final Context ctx;
    private final Question qn;
    private final int mWidth;
    private final int mHeight;

    AnswerAdapter(Context ctx, Question qn) {
        this.ctx = ctx;
        this.qn = qn;
        this.mHeight = ctx.getResources().getDimensionPixelSize(R.dimen.appunfold_answer_item_height);
        this.mWidth = ctx.getResources().getDimensionPixelSize(R.dimen.appunfold_answer_item_width);
    }

    @Override
    public int getCount() {
        return qn.descList.size();
    }

    @Override
    public Object getItem(int position) {
        return qn.descList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FrameLayout descItem = (FrameLayout) convertView;
        if (descItem == null) {
            LayoutInflater li = LayoutInflater.from(ctx);
            descItem = (FrameLayout) li.inflate(R.layout.uiq_answer_item, parent, false);
        } else {
            descItem.removeAllViews();
        }

        QDesc qDesc = qn.descList.get(position);

        switch (qDesc.type) {
            case rtf: {
                handleRTF(descItem, qDesc.value);
                break;
            }
            case image: {
                try {
                    handleImage(descItem, qDesc.value);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            }
            case wt:
                try {
                    SyncData syncData = UserIQSDKInternal.getSyncData();
                    Walkthrough walkthrough = syncData.getWalkthroughById(qDesc.value);
                    if (walkthrough != null)
                        handleWalkthrough(descItem, walkthrough);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }

        return descItem;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    private void handleRTF(FrameLayout descItem, String value) {
        boolean needsBottomPadding = value.startsWith("<p>");
        value = value.replaceAll("<p>", "");
        value = value.replaceAll("</p>", "");
        TextView textView = new TextView(ctx);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setTextColor(ctx.getResources().getColor(R.color.grey));

        if(needsBottomPadding) {
            textView.setPadding(0, 0, 0, dpToPx(10));
        }

        if (SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(value, FROM_HTML_MODE_COMPACT, null, new ListTagHandler()));
        } else {
            textView.setText(Html.fromHtml(value, null, new ListTagHandler()));
        }


        descItem.addView(textView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void handleImage(FrameLayout descItem, String value) throws FileNotFoundException {
        File assetFile = UserIQSDKInternal.getInstance().getAsset(value);
        if (assetFile == null) {
            logger.e(value + " : Image not found", null);
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(assetFile));
        ImageView imageView = new ImageView(ctx);
        imageView.setImageBitmap(bitmap);
        imageView.setAdjustViewBounds(true);
        if (SDK_INT >= LOLLIPOP) {
            imageView.setElevation(dpToPx(4));
        }
        //  Set<String> keys = DataRepository.getAppData().screens.keySet();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                , ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = dpToPx(12);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        descItem.addView(imageView, layoutParams);
    }

    private void handleWalkthrough(FrameLayout descItem, Walkthrough walkthrough) throws FileNotFoundException {
        LinearLayout wtContainer = new LinearLayout(ctx);
        wtContainer.setOrientation(LinearLayout.VERTICAL);
        long start = System.currentTimeMillis();

        for (int i = 0; i < walkthrough.steps.size(); i++) {
            FrameLayout frameLayout = new FrameLayout(ctx);
            int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, ctx.getResources().getDisplayMetrics());
            LinearLayout hostLL = new LinearLayout(ctx);
            hostLL.setBackgroundColor(Color.parseColor("#ebecf0"));
//            hostLL.setBackground(ctx.getResources().getDrawable(R.drawable.ctx_bg_round));
            hostLL.setPadding(padding, padding, padding, padding);
            hostLL.setOrientation(LinearLayout.VERTICAL);
            hostLL.setGravity(Gravity.CENTER_HORIZONTAL);
            TextView tvStep = new TextView(ctx);
            tvStep.setGravity(Gravity.CENTER_HORIZONTAL);
            tvStep.setTextColor(Color.BLACK);
            tvStep.setTextSize(16);
            tvStep.setText(UserIQSDKInternal.getInstance().getResources().getString(R.string.appunfold_step, i + 1));
            tvStep.setPadding(0, 8, 0, 14);
            hostLL.addView(tvStep);

            WTStep wtStep = walkthrough.steps.get(i);

            File assetFile = UserIQSDKInternal.getInstance().getAsset(wtStep.imgId);
            if (assetFile == null) {
                logger.e("asset " + wtStep.imgId + " not found", null);
                return;
            }
            Bitmap screenBitmap = BitmapFactory.decodeStream(new FileInputStream(assetFile));
            ImageView ivScreen = new ImageView(ctx);
            ivScreen.setScaleType(ImageView.ScaleType.FIT_XY);
            ivScreen.setImageBitmap(screenBitmap);
            final Rect tempScreenBounds = new Rect(0, 0, mWidth, mHeight);
            final int tipHeight = UserIQSDKInternal.getInstance().getResources().getDimensionPixelSize(R.dimen.appunfold_tipbox_arrow_height);
            final int tipWidth = UserIQSDKInternal.getInstance().getResources().getDimensionPixelSize(R.dimen.appunfold_tipbox_arrow_width);
            Rect anchorBounds = wtStep.element.bounds;
            Rect newAnchorBounds = new Rect(anchorBounds);
            newAnchorBounds.top *= (float) mHeight / wtStep.imgHeight;
            newAnchorBounds.bottom *= (float) mHeight / wtStep.imgHeight;
            newAnchorBounds.right *= (float) mWidth / wtStep.imgWidth;
            newAnchorBounds.left *= (float) mWidth / wtStep.imgWidth;

            StaticOverlay overlay = new StaticOverlay(
                    ctx,
                    newAnchorBounds,
                    Color.parseColor("#66000000"),
                    Color.parseColor("#DD2f8282"),
                    false
            );

            ToolTipViewOld toolTipView = new ToolTipViewOld(
                    ctx,
                    newAnchorBounds,
                    tempScreenBounds,
                    tipHeight,
                    tipWidth,
                    wtStep.desc,
                    Color.parseColor("#2f8282"),
                    Color.parseColor("#DD2f8282"),
                    Color.parseColor("#ffffff"),
                    12);

//            if (SDK_INT >= LOLLIPOP) {
//                toolTipView.setElevation(32);
//            }

            frameLayout.addView(ivScreen, new FrameLayout.LayoutParams(mWidth, mHeight));
            frameLayout.addView(overlay, new FrameLayout.LayoutParams(mWidth, mHeight));
            frameLayout.addView(toolTipView, toolTipView.getFrameLayoutParams());
            hostLL.addView(frameLayout);
        /*TextView textView = new TextView(ctx);
        textView.setText(UserIQSDKInternal.getContext().getString(R.string.step, i + 1));
        textView.setGravity(Gravity.CENTER);
        frameLayout.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        */
            int specWidth = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int specHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            hostLL.measure(specWidth, specHeight);
            Bitmap b = Bitmap.createBitmap(hostLL.getMeasuredWidth(), hostLL.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            //Bitmap b = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            hostLL.layout(0, 0, hostLL.getMeasuredWidth(), hostLL.getMeasuredHeight());
            //  frameLayout.layout(0, 0, mWidth, mHeight);
            hostLL.draw(c);

            WalkthroughImage walkthroughImage = new WalkthroughImage(ctx, b);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(hostLL.getMeasuredWidth(), hostLL.getMeasuredHeight());
            layoutParams.topMargin = UserIQSDKInternal.getInstance().getResources().getDimensionPixelOffset(R.dimen.appunfold_answer_item_top_margin);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            walkthroughImage.setBackgroundColor(Color.WHITE);
            if (SDK_INT >= LOLLIPOP) {
                int elevation = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, ctx.getResources().getDisplayMetrics());
                walkthroughImage.setElevation(elevation);
            }
            wtContainer.addView(walkthroughImage, layoutParams);
        }

        logger.d("loadWT=" + (System.currentTimeMillis() - start) + "ms");

        descItem.addView(wtContainer);
    }

    private static class StaticOverlay extends ShowcaseView {
        public StaticOverlay(@NonNull Context context, Rect elBounds, int bgColor, int borderColor, boolean dashedBorder) {
            super(context, Collections.singletonList(new Element(Element.RECT, elBounds, "")),
                    0, bgColor, borderColor, dashedBorder);
            setVisibility(VISIBLE);
            setBorderWidth(dpToPx(2));
        }
    }
}
