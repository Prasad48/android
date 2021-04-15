package com.useriq.sdk.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.useriq.Logger;
import com.useriq.sdk.R;
import com.useriq.sdk.ViewUtils;
import com.useriq.sdk.models.EditText;
import com.useriq.sdk.models.Image;
import com.useriq.sdk.models.NPSNode;
import com.useriq.sdk.models.Node;
import com.useriq.sdk.models.RatingNode;
import com.useriq.sdk.models.Text;
import com.useriq.sdk.models.UINode;
import com.useriq.sdk.models.V1Modal;
import com.useriq.sdk.v1Modal.CustomLayout;
import com.useriq.sdk.v1Modal.ImageAlignUtil;

import java.util.List;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.useriq.sdk.models.Button.CLOSE;
import static com.useriq.sdk.models.Button.OPEN_URL;
import static com.useriq.sdk.models.Button.SUBMIT;
import static com.useriq.sdk.models.Text.OPEN_MODAL;
import static com.useriq.sdk.models.Text.QUESTION;
import static com.useriq.sdk.models.Text.WEBVIEW;
import static com.useriq.sdk.models.Text.WT;

public class ViewFactory {

    private static Logger logger = Logger.init(ViewFactory.class.getSimpleName());
    private static TextView prevButton[] = new TextView[1];
    private static final String[] type = new String[1];
    private static String response = "";

    public static View createStarRatingView(Context context, RatingNode starRatingNode, final CustomLayout.ClickHandler clickHandler) {
        return starRatingNode.buildView(context, clickHandler);
    }

    public static View createNPSView(Context context, NPSNode npsNode, final CustomLayout.ClickHandler clickHandler, V1Modal modal) {
        return npsNode.buildView(context, clickHandler, modal);
    }

    public static View createNode(Context context, Node node, CustomLayout.ImageProvider imageProvider, CustomLayout.ClickHandler clickHandler, V1Modal modal) {
        CustomLayout vg = new CustomLayout(context, node, imageProvider, clickHandler, modal);
        vg.setClickable(true);
        setDrawable(vg, node);
        if (node.id.equals("frame") && SDK_INT > LOLLIPOP) {
            Resources res = context.getResources();
            vg.setTransitionName(res.getString(R.string.transition_morph_view));
            int bgColor = Color.argb(
                    node.bgColor.get(0),
                    node.bgColor.get(1),
                    node.bgColor.get(2),
                    node.bgColor.get(3));
            vg.setTag(R.id.viewBgColor, bgColor);
            vg.setTag(R.id.viewRadius, node.borderRadius.get(0));
        }
        return vg;
    }

    public static View createCrossButton(final Context context, final Text uiNode, final CustomLayout.ClickHandler clickHandler) {
        TextView crossButton = new TextView(context);
        crossButton.setText(uiNode.getText());
        if (uiNode.getColor() != null) {
            crossButton.setTextColor(Color.argb(uiNode.getColor().get(0), uiNode.getColor().get(1),
                    uiNode.getColor().get(2), uiNode.getColor().get(3)));
        }
        if (uiNode.id != null) {
            crossButton.setId(uiNode.id.hashCode());
        }
        crossButton.setTextSize(uiNode.getTextSize());

        crossButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickHandler.trackButton(uiNode.id);
                clickHandler.close();
            }
        });
        setDrawable(crossButton, uiNode);
        return crossButton;
    }

    public static View createImage(Context context, final Image imageNode, final Bitmap image) {
        if (image == null) {
            logger.e("Image can't be null for imageView. ", null);
            return null;
        }

        final ImageView imageView = new ImageView(context);
        if (imageNode.id != null) {
            imageView.setId(imageNode.id.hashCode());
        }
        imageView.setImageBitmap(image);

        imageView.setScaleType(ImageView.ScaleType.MATRIX);

        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                StringBuilder stringBuilder = new StringBuilder();
                if (imageNode.alignX.equals(UINode.RIGHT))
                    stringBuilder.append("xMax");
                else if (imageNode.alignX.equals(UINode.CENTER)) {
                    stringBuilder.append("xMid");
                }

                if (imageNode.alignY.equals(UINode.BOTTOM))
                    stringBuilder.append("yMax");
                else if (imageNode.alignY.equals(UINode.CENTER)) {
                    stringBuilder.append("yMid");
                }
                RectF vbRect = new RectF(0, 0, imageView.getWidth(), imageView.getHeight());
                RectF eRect = new RectF(0, 0, image.getWidth(), image.getHeight());
                Matrix matrix = ImageAlignUtil.getTransform(eRect, vbRect, stringBuilder.toString(), imageNode.crop ? ImageAlignUtil.MOS_SLICE : ImageAlignUtil.MOS_MEET, false);
                imageView.setImageMatrix(matrix);

            }
        });
        setDrawable(imageView, imageNode);
        return imageView;
    }

    public static View createText(final Context context, final Text textNode, final CustomLayout.ClickHandler clickHandler) {
        TextView textView = new TextView(context);
        textView.setText(textNode.getText());
        int gravity = 0;
        String alignX = textNode.getAlignX();
        switch (alignX) {
            case UINode.LEFT:
                gravity |= Gravity.START;
                break;
            case UINode.CENTER:
                gravity |= Gravity.CENTER_HORIZONTAL;
                break;
            case UINode.RIGHT:
                gravity |= Gravity.RIGHT;
                break;
        }

        String alignY = textNode.getAlignX();
        switch (alignY) {
            case UINode.CENTER:
                gravity |= Gravity.CENTER_VERTICAL;
                break;
            case UINode.TOP:
                gravity |= Gravity.TOP;
                break;
            case UINode.BOTTOM:
                gravity |= Gravity.BOTTOM;
                break;
        }
        textView.setGravity(gravity);
        textView.setTextSize(textNode.getTextSize());
        if (textNode.getShadowColor() != null) {
            List<Integer> shadowColor = textNode.getShadowColor();
            textView.setShadowLayer(textNode.getShadowRadius(), textNode.getShadowDx(), textNode.getShadowDy(), Color.argb(
                    shadowColor.get(0), shadowColor.get(1),
                    shadowColor.get(2), shadowColor.get(3)
            ));
        }
        if (textNode.id != null) {
            textView.setId(textNode.id.hashCode());
        }
        if (textNode.getColor() != null) {
            List<Integer> color = textNode.getColor();
            textView.setTextColor(Color.argb(color.get(0), color.get(1), color.get(2), color.get(3)));
        }
        textView.setClickable(true);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickHandler.trackButton(textNode.id);
                int action = textNode.getAction();
                switch (action) {
                    case SUBMIT: {
                        clickHandler.onSubmit(response);
                        break;
                    }
                    case CLOSE: {
                        clickHandler.close();
                        break;
                    }
                    case OPEN_URL: {
                        clickHandler.openURL(textNode.getUrl());
                        break;
                    }
                    case WT: {
                        clickHandler.showWalkthrough(textNode.getValue());
                        break;
                    }
                    case QUESTION: {
                        clickHandler.showQuestion(textNode.getValue());
                        break;
                    }
                    case WEBVIEW: {
                        clickHandler.openWebview(textNode.getUrl());
                        break;
                    }
                    case OPEN_MODAL: {
                        clickHandler.openModalview(textNode.getValue());
                        break;
                    }
                }
            }
        });

        setDrawable(textView, textNode);
        return textView;
    }

    public static View createEditText(Context context, final EditText editTextNode, final CustomLayout.ClickHandler clickHandler) {
        android.widget.EditText editTextView = new android.widget.EditText(context);

        editTextView.setTextSize(editTextNode.getTextSize());
        if (editTextNode.id != null) {
            editTextView.setId(editTextNode.id.hashCode());
        }
        if (editTextNode.getColor() != null) {
            List<Integer> color = editTextNode.getColor();
            editTextView.setTextColor(Color.argb(color.get(0), color.get(1), color.get(2), color.get(3)));
        } else {
            editTextView.setTextColor(Color.BLACK);
        }
        editTextView.setSingleLine();
        editTextView.setPadding(15, 0, 15, 0);

        if (editTextNode.getPlaceholderColor() != null) {
            List<Integer> color = editTextNode.getPlaceholderColor();
            editTextView.setHintTextColor(Color.argb(color.get(0), color.get(1), color.get(2), color.get(3)));
        } else {
            editTextView.setHintTextColor(Color.argb(155, 155, 155, 61));
        }

        editTextView.setHint(editTextNode.getPlaceholder());
        editTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                response = s.toString();
            }
        });
        editTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_NEXT) {
                    clickHandler.onSubmit(response);
                    return true;
                }

                return false;
            }
        });
        setDrawable(editTextView, editTextNode);
        return editTextView;
    }

    public static View createBackdrop(Context ctx, final CustomLayout.ClickHandler clickHandler) {
        FrameLayout backdrop = new FrameLayout(ctx);
        backdrop.setId("backdrop".hashCode());
        if (SDK_INT >= LOLLIPOP) {
            backdrop.setTransitionName(ctx.getResources().getString(R.string.transition_fade_view));
        }
        backdrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickHandler.close();
            }
        });
        return backdrop;
    }

    private static void setDrawable(View view, UINode node) {
        Drawable bgDrawable = ViewUtils.getBgDrawable(node.bgColor, node.borderColor, node.borderWidth, node.borderRadius);
        view.setBackground(bgDrawable);
    }
}

