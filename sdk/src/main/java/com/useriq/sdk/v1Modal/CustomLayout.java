package com.useriq.sdk.v1Modal;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.useriq.sdk.UserIQSDKInternal;
import com.useriq.sdk.ViewUtils;
import com.useriq.sdk.models.EditText;
import com.useriq.sdk.models.Image;
import com.useriq.sdk.models.NPSNode;
import com.useriq.sdk.models.Node;
import com.useriq.sdk.models.RatingNode;
import com.useriq.sdk.models.Text;
import com.useriq.sdk.models.UINode;
import com.useriq.sdk.models.V1Modal;
import com.useriq.sdk.util.UnitUtil;
import com.useriq.sdk.views.ViewFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CustomLayout extends ViewGroup {
    static final Operator ADD = new Operator('+', 0, Operator.ASSOC_LEFT, 2) {
        @Override
        public float eval(Rect parent, float a, float b, boolean isHorizontal, float screenDensity) {
            return a + b;
        }
    };

    static final Operator SUB = new Operator('-', 0, Operator.ASSOC_LEFT, 2) {
        @Override
        public float eval(Rect parent, float a, float b, boolean isHorizontal, float screenDensity) {
            return a - b;
        }
    };
    static final Operator PERC = new Operator('%', 1, Operator.ASSOC_LEFT, 1) {
        @Override
        public float eval(Rect parent, float a, float b, boolean isHorizontal, float screenDensity) {
            if (isHorizontal) {
                return (parent.width() * a * 0.01f) / screenDensity;
            }
            return (parent.height() * a * 0.01f) / screenDensity;
        }
    };

    static boolean DEBUG = true;

    UINode uiNode;
    V1Modal modal;
    SparseArray<Rect> renderedRects, parentsRenderedRects;
    Rect parentRect, currentRect;
    float screenDensity;
    ImageProvider imageProvider;
    ClickHandler clickHandler;
    Path clipPath;
    RectF clipRectF;
    Rect clipRect;
    float[] cornerRadius;
    boolean isContainingComment;

    public CustomLayout(Context context, Node node, ImageProvider imageProvider, ClickHandler clickHandler, V1Modal modal) {
        super(context);
        setWillNotDraw(false);
        this.uiNode = node;
        this.modal = modal;
        this.imageProvider = imageProvider;
        this.clickHandler = clickHandler;
        renderedRects = new SparseArray<>(node.children.length);
        screenDensity = context.getResources().getDisplayMetrics().density;
        clipPath = new Path();
        clipRect = new Rect();
        clipRectF = new RectF();
        List<Integer> borderRadius = node.borderRadius;
        if (borderRadius != null) {
            cornerRadius = new float[]{borderRadius.get(0), borderRadius.get(0), borderRadius.get(1), borderRadius.get(1), borderRadius.get(2), borderRadius.get(2), borderRadius.get(3), borderRadius.get(3)};
        } else {
            cornerRadius = new float[]{0, 0, 0, 0, 0, 0, 0, 0};
        }
        for (int i = 0; i < node.children.length; i++) {
            if (node.children[i].id != null) {
                renderedRects.put(node.children[i].id.hashCode(), new Rect());
            }
        }

        Drawable bgDrawable = ViewUtils.getBgDrawable(node.bgColor, node.borderColor, node.borderWidth, node.borderRadius);
        setBackground(bgDrawable);
        parentRect = new Rect();
        currentRect = new Rect();
        addChildren(node.children);
    }

    public void addChildren(UINode[] children) {
        for (UINode child : children) {
            View currentView = null;
            switch (child.type) {
                case UINode.BUTTON:
                case UINode.TEXT_VIEW:
                    if (child.id.equals("outsideClick")) {
                        currentView = ViewFactory.createBackdrop(getContext(), clickHandler);
                    } else if (child.id.equals("cross")) {
                        currentView = ViewFactory.createCrossButton(getContext(), (Text) child, clickHandler);
                    } else {
                        currentView = ViewFactory.createText(getContext(), (Text) child, clickHandler);
                    }
                    break;
                case UINode.EDIT_TEXT:
                    isContainingComment = true;
                    currentView = ViewFactory.createEditText(getContext(), (EditText) child, clickHandler);
                    break;
                case UINode.IMAGE_VIEW:
                    Image imageNode = (Image) child;
                    currentView = ViewFactory.createImage(getContext(), imageNode, imageProvider.getImageById(imageNode.assetId));
                    break;
                case UINode.NODE:
                    currentView = ViewFactory.createNode(getContext(), (Node) child, imageProvider, clickHandler, modal);
                    break;
                case UINode.RATING:
                    currentView = ViewFactory.createStarRatingView(getContext(), (RatingNode) child, clickHandler);
                    break;
                case UINode.NPS:
                    currentView = ViewFactory.createNPSView(getContext(), (NPSNode) child, clickHandler, modal);
            }
            if (currentView != null) {
                addView(currentView, new CustomParams(child));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && child.shadowColor != null) {
                    currentView.setElevation(child.shadowDx);
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.getClipBounds(clipRect);
        clipRectF.set(clipRect.left, clipRect.top, clipRect.right, clipRect.bottom);
        clipPath.addRoundRect(clipRectF, cornerRadius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!(getParent() instanceof ViewGroup)) logError("parent is not present for view");
        if (getParent() instanceof CustomLayout) {
            parentRect = ((CustomLayout) getParent()).currentRect;
            parentsRenderedRects = ((CustomLayout) getParent()).renderedRects;
        } else {
            parentRect.set(0, 0, MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        }
        float left = RPN.parse(uiNode.l).eval(parentRect, parentsRenderedRects, true, screenDensity);
        float right = RPN.parse(uiNode.r).eval(parentRect, parentsRenderedRects, true, screenDensity);
        float bottom = RPN.parse(uiNode.b).eval(parentRect, parentsRenderedRects, false, screenDensity);
        float top = RPN.parse(uiNode.t).eval(parentRect, parentsRenderedRects, false, screenDensity);
        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec((int) (right - left), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) (bottom - top), MeasureSpec.EXACTLY));
    }

    static void logError(String message) {
        Log.e(CustomLayout.class.getSimpleName(), message);
        if (DEBUG)
            throw new IllegalArgumentException(message);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        currentRect.set(l, t, r, b);
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            CustomParams lp = (CustomParams) child.getLayoutParams();
            int left = (int) lp.left.eval(currentRect, renderedRects, true, screenDensity);
            int right = (int) lp.right.eval(currentRect, renderedRects, true, screenDensity);
            int top = (int) lp.top.eval(currentRect, renderedRects, false, screenDensity);
            int bottom = (int) lp.bottom.eval(currentRect, renderedRects, false, screenDensity);
            int width = MeasureSpec.makeMeasureSpec((right - left), MeasureSpec.EXACTLY);
            int height = MeasureSpec.makeMeasureSpec((bottom - top), MeasureSpec.EXACTLY);
            if (child instanceof CustomLayout && ((CustomLayout) child).uiNode.id.equals("frame")) {
                int availableW = ViewUtils.getScreenWidth();
                int availableH = ViewUtils.getScreenHeight();
                switch (modal.type) {
                    case V1Modal.MODAL_TYPE_inAppHeader: {
                        boolean isNavHidden = false;
                        if (UserIQSDKInternal.getCurrActivity() != null) {
                            int uiVisibility = UserIQSDKInternal.getCurrActivity().getWindow().getDecorView().getSystemUiVisibility();
                            isNavHidden = ((uiVisibility | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == uiVisibility);
                        }
                        child.measure(width, height);
                        if (right - left > 2000 || bottom - top > 2000) {
                            child.layout(availableW / 2 - UnitUtil.dpToPx(300), top, availableW / 2 + UnitUtil.dpToPx(300), bottom);
                        } else {
                            if (!isNavHidden && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                                int navBarHeight = 0;
                                if (resourceId > 0) {
                                    navBarHeight = getResources().getDimensionPixelSize(resourceId);
                                }
                                //FIXME: handle for navBar visibility
                                child.layout(left, top, right, UnitUtil.dpToPx(150));
                            } else {
                                child.layout(left, top, right, UnitUtil.dpToPx(150));
                            }
                        }
                    }
                    break;
                    case V1Modal.MODAL_TYPE_npsHeader: {
                        child.measure(width, height);
                        int cappingW = 0;
                        int le = 0;
                        int ri = availableW;
//                        if (UnitUtil.pxToDp(availableW) > 420) {
//                            cappingW = UnitUtil.dpToPx(420);
//                            int gap = (availableW - cappingW);
//                            le = gap / 2;
//                            ri = availableW - gap / 2 ;
//                        }
                        if (isContainingComment)
                            child.layout(le, top, ri, UnitUtil.dpToPx(270));
                        else
                            child.layout(le, top, ri, UnitUtil.dpToPx(250));
                    }
                    break;
                    case V1Modal.MODAL_TYPE_starHeader: {
                        boolean isNavHidden = false;
                        if (UserIQSDKInternal.getCurrActivity() != null) {
                            int uiVisibility = UserIQSDKInternal.getCurrActivity().getWindow().getDecorView().getSystemUiVisibility();
                            isNavHidden = ((uiVisibility | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == uiVisibility);
                        }
                        child.measure(width, height);
                        if (right - left > 2000 || bottom - top > 2000) {
                            child.layout(availableW / 2 - UnitUtil.dpToPx(300), top, availableW / 2 + UnitUtil.dpToPx(300), bottom);
                        } else {
                            if (isContainingComment)
                                child.layout(left, top, right, UnitUtil.dpToPx(220));
                            else
                                child.layout(left, top, right, UnitUtil.dpToPx(200));
//                            if (!isNavHidden && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
//                                int navBarHeight = 0;
//                                if (resourceId > 0) {
//                                    navBarHeight = getResources().getDimensionPixelSize(resourceId);
//                                }
//                                child.layout(left, top, right, UnitUtil.dpToPx(200));
//                            } else {
//                                child.layout(left, top, right, UnitUtil.dpToPx(200));
//                            }
                        }
                    }
                    break;
                    case V1Modal.MODAL_TYPE_starDefault: {
                        child.measure(width, height);
                        int cappingW = ((availableW / 4) * 3) / 2;
                        if (cappingW > UnitUtil.dpToPx(150)) {
                            cappingW = UnitUtil.dpToPx(150);
                        }
                        int cappingH = ((availableH / 4) * 3) / 2;
                        if (cappingH > UnitUtil.dpToPx(150)) {
                            cappingH = UnitUtil.dpToPx(150);
                        }
                        int le = (availableW / 2) - cappingW;
                        int to = (availableH / 2) - cappingH;
                        int ri = (availableW / 2) + cappingW;
                        int bo = (availableH / 2) + cappingH;
                        if (isContainingComment)
                            child.layout(le, to - 10, ri, bo + 10);
                        else
                            child.layout(le, to, ri, bo);
                    }
                    break;
                    case V1Modal.MODAL_TYPE_npsDefault: {
                        child.measure(width, height);
                        int cappingW = ((availableW / 4) * 3) / 2;
                        if (cappingW > UnitUtil.dpToPx(160)) {
                            cappingW = UnitUtil.dpToPx(160);
                        }
                        int cappingH = ((availableH / 4) * 3) / 2;
                        if (cappingH > UnitUtil.dpToPx(160)) {
                            cappingH = UnitUtil.dpToPx(160);
                        }
                        int le = (availableW / 2) - cappingW;
                        int to = (availableH / 2) - cappingH;
                        int ri = (availableW / 2) + cappingW;
                        int bo = (availableH / 2) + cappingH;
                        if (isContainingComment)
                            child.layout(le, to - 10, ri, bo + 10);
                        else
                            child.layout(le, to, ri, bo);
                    }
                    break;
                    case V1Modal.MODAL_TYPE_inAppDefault: {
                        child.measure(width, height);
                        int cappingW = ((availableW / 4) * 3) / 2;
                        if (cappingW > UnitUtil.dpToPx(150)) {
                            cappingW = UnitUtil.dpToPx(150);
                        }
                        int cappingH = ((availableH / 4) * 3) / 2;
                        if (cappingH > UnitUtil.dpToPx(180)) {
                            cappingH = UnitUtil.dpToPx(180);
                        }
                        int le = (availableW / 2) - cappingW;
                        int to = (availableH / 2) - cappingH;
                        int ri = (availableW / 2) + cappingW;
                        int bo = (availableH / 2) + cappingH;
                        child.layout(le, to, ri, bo);
                    }
                    break;
                    case V1Modal.MODAL_TYPE_npsFooter: {
                        child.measure(width, height);
                        int cappingW = 0;
                        int le = 0;
                        int ri = availableW;
//                        if (UnitUtil.pxToDp(availableW) > 420) {
//                            cappingW = UnitUtil.dpToPx(420);
//                            int gap = (availableW - cappingW);
//                            le = gap / 2;
//                            ri = availableW - gap / 2 ;
//                        }
                        if (isContainingComment)
                            child.layout(le, bottom - UnitUtil.dpToPx(270), ri, bottom);
                        else
                            child.layout(le, bottom - UnitUtil.dpToPx(250), ri, bottom);
                    }
                    break;
                    case V1Modal.MODAL_TYPE_inAppFooter: {
                        boolean isNavHidden = false;
                        if (UserIQSDKInternal.getCurrActivity() != null) {
                            int uiVisibility = UserIQSDKInternal.getCurrActivity().getWindow().getDecorView().getSystemUiVisibility();
                            isNavHidden = ((uiVisibility | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == uiVisibility);
                        }
                        child.measure(width, height);
                        if (right - left > 2000) {
                            child.layout(availableW / 2 - UnitUtil.dpToPx(300), bottom - UnitUtil.dpToPx(250), availableW / 2 + UnitUtil.dpToPx(300), bottom);
                        } else {
                            if (!isNavHidden && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                                int navBarHeight = 0;
                                if (resourceId > 0) {
                                    navBarHeight = getResources().getDimensionPixelSize(resourceId);
                                }
                                child.layout(left, availableH - UnitUtil.dpToPx(250), right, bottom);
                            } else {
                                child.layout(left, bottom - UnitUtil.dpToPx(230), right, bottom);
                            }
                        }
                    }
                    break;
                    case V1Modal.MODAL_TYPE_starFooter: {
                        boolean isNavHidden = false;
                        if (UserIQSDKInternal.getCurrActivity() != null) {
                            int uiVisibility = UserIQSDKInternal.getCurrActivity().getWindow().getDecorView().getSystemUiVisibility();
                            isNavHidden = ((uiVisibility | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == uiVisibility);
                        }
                        child.measure(width, height);
                        if (right - left > 2000) {
                            child.layout(availableW / 2 - UnitUtil.dpToPx(300), bottom - UnitUtil.dpToPx(250), availableW / 2 + UnitUtil.dpToPx(300), bottom);
                        } else {
                            if (!isNavHidden && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                                int navBarHeight = 0;
                                if (resourceId > 0) {
                                    navBarHeight = getResources().getDimensionPixelSize(resourceId);
                                }
                                top = availableH - UnitUtil.dpToPx(250);
                            } else {
                                top = bottom - UnitUtil.dpToPx(230);
                            }
                            if (isContainingComment)
                                child.layout(left, top - 20, right, bottom);
                            else
                                child.layout(left, top, right, bottom);
                        }
                    }
                    break;
                    default: {
                        child.measure(width, height);
                        child.layout(left, top, right, bottom);
                    }
                }
            } else {
                child.measure(width, height);
                child.layout(left, top, right, bottom);
            }

            Rect rect = renderedRects.get(child.getId());
            if (rect != null) {
                rect.set(left, top, right, bottom);
                renderedRects.put(child.getId(), rect);
            }
        }
    }

    public interface ImageProvider {
        Bitmap getImageById(String assetId);
    }

    public interface ClickHandler {
        void onStarRatingClick(String rating);

        void trackButton(String buttonId);

        void close();

        void openURL(String url);

        void onNpsClick(String id);

        void onSubmit(String response);

        void showWalkthrough(String wtId);

        void showQuestion(String quesId);

        void openWebview(String url);

        void openModalview(String value);
    }

    private static class CustomParams extends LayoutParams {
        RPN left, right, bottom, top;

        private CustomParams(UINode uiNode) {
            super(0, 0);
            left = RPN.parse(uiNode.l);
            bottom = RPN.parse(uiNode.b);
            top = RPN.parse(uiNode.t);
            right = RPN.parse(uiNode.r);
        }
    }

    private static class RPN {
        private ArrayList<Object> list;

        public RPN(ArrayList<Object> list) {
            this.list = list;
        }

        public static RPN parse(String str) {
            if (str == null || str.length() == 0) {
                return null;
            }
            TokenReader tr = new TokenReader(str);
            ArrayList<Object> queue = new ArrayList<>();
            Stack<Operator> stack = new Stack<>();
            Object token;
            while ((token = tr.readToken()) != null) {
                if (token instanceof Number) {
                    queue.add(token);
                } else if (token instanceof Ref) {
                    queue.add(token);
                } else if (token instanceof Operator) {
                    Operator operator = (Operator) token;
                    while (!stack.empty()) {
                        Operator o2 = stack.peek();
                        if ((operator.assoc == Operator.ASSOC_LEFT && operator.prec <= o2.prec)
                                || (operator.assoc == Operator.ASSOC_RIGHT && operator.prec < o2.prec)) {
                            queue.add(stack.pop());
                        } else {
                            break;
                        }
                    }
                    stack.push(operator);
                } else {
                    logError("Invalid token found. (returning null)");
                    return null;
                }
            }
            while (!stack.empty()) {
                queue.add(stack.pop());
            }
            if (queue.isEmpty()) {
                return null;
            } else {
                return new RPN(queue);
            }
        }

        public float eval(Rect parent, SparseArray<Rect> measuredRects, boolean isHorizontal, float screenDensity) {
            float[] stack = new float[list.size()];
            int sn = 0;

            for (Object obj : list) {
                if (obj instanceof Operator) {
                    Operator op = (Operator) obj;
                    if (sn < op.argc) {
                        logError("Illegal number of arguments: " + op.argc);
                    }
                    float a = Float.NaN, b = Float.NaN;
                    if (op.argc == 0) {
                    } else if (op.argc == 1) {
                        a = stack[--sn];
                    } else if (op.argc == 2) {
                        b = stack[--sn];
                        a = stack[--sn];
                    } else {
                        logError("Illegal number of arguments: " + op.argc);
                    }
                    float c = op.eval(parent, a, b, isHorizontal, screenDensity);
                    stack[sn++] = c;
                } else if (obj instanceof Float) {
                    stack[sn++] = ((Float) obj).floatValue();
                } else if (obj instanceof Ref) {
                    float f = ((Ref) obj).eval(parent, measuredRects, screenDensity);
                    stack[sn++] = f;
                } else {
                    logError("unknown token");
                }
            }

            if (sn != 1) {
                logError("syntax error");
            }
            return stack[0] * screenDensity;
        }
    }

    private static class TokenReader {
        private char[] chars;
        private int n;
        private int i;
        private String orig;

        TokenReader(String str) {
            this.orig = str;
            this.chars = str.toCharArray();
            this.n = str.length();
            this.i = 0;
        }

        public Object readToken() {
            StringBuilder num = null, ref = null;
            int propPos = -1;
            while (i < n) {
                char c = chars[i];
                if (num == null && ref == null) {
                    if (c >= '0' && c <= '9' || c == '.') {
                        num = new StringBuilder("");
                        num.append(c);
                    } else if ((c >= 'a' && c <= 'z') || c == '_' || (c >= 'A' && c <= 'Z') || c == '$') {
                        ref = new StringBuilder("");
                        ref.append(c);
                    } else if (c == PERC.symbol) {
                        i++;
                        return PERC;
                    } else if (c == ADD.symbol) {
                        i++;
                        return ADD;
                    } else if (c == SUB.symbol) {
                        i++;
                        return SUB;
                    } else if (c == ' ' || c == '\t') {
                    } else {
                        logError("token not found (returning null): " + c);
                        return null;
                    }
                } else if (num != null) {
                    if (c >= '0' && c <= '9' || c == '.') {
                        num.append(c);
                    } else {
                        return Float.parseFloat(num.toString());
                    }
                } else if (ref != null) {
                    if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || c == '_' || (c >= 'A' && c <= 'Z') || c == '$') {
                        ref.append(c);
                    } else if (c == '.') {
                        propPos = ref.length();
                        ref.append(c);
                    } else {
                        return parseReference(ref, propPos);
                    }
                }
                i++;
            }
            if (num != null) {
                return Float.parseFloat(num.toString());
            }
            if (ref != null) {
                return parseReference(ref, propPos);
            }
            return null;
        }

        private Ref parseReference(StringBuilder ref, int propPos) {
            if (propPos == -1) {
                logError("Invalid reference string: " + ref.toString() + " (returning null)");
                return null;
            }
            String refT = ref.substring(0, propPos);
            String refP = ref.substring(propPos + 1);
            int refTarget = -1, refProp = -1;
            if ("$parent".equals(refT)) {
                refTarget = Ref.TARGET_PARENT;
            } else {
                refTarget = refT.hashCode();
            }
            if ("l".equals(refP)) {
                refProp = Ref.PROP_LEFT;
            } else if ("t".equals(refP)) {
                refProp = Ref.PROP_TOP;
            } else if ("r".equals(refP)) {
                refProp = Ref.PROP_RIGHT;
            } else if ("b".equals(refP)) {
                refProp = Ref.PROP_BOTTOM;
            } else {
                logError(refT + " : " + refP + " not found. (returning null)");
                return null;
            }
            return new Ref(refTarget, refProp, refT + " : " + refP);
        }
    }

    private abstract static class Operator {
        public static final int ASSOC_LEFT = 1;
        public static final int ASSOC_RIGHT = 2;

        public final int prec;
        public final int assoc;
        public final int argc;
        public final char symbol;

        protected Operator(char symbol, int prec, int assoc, int argc) {
            this.prec = prec;
            this.assoc = assoc;
            this.argc = argc;
            this.symbol = symbol;
        }

        public abstract float eval(Rect rect, float a, float b, boolean isHorizontal, float screenDensity);
    }

    private static class Ref {
        public static final int PROP_LEFT = 0;
        public static final int PROP_TOP = 1;
        public static final int PROP_RIGHT = 2;
        public static final int PROP_BOTTOM = 3;

        public static final int TARGET_PARENT = 0;
        public final int target;
        public final int property;
        public final String debugInfo;

        public Ref(int target, int prop, String debugInfo) {
            this.target = target;
            this.property = prop;
            this.debugInfo = debugInfo;
        }

        public float eval(Rect parent, SparseArray<Rect> measuredRects, float screenDensity) {
            Rect rect;
            if (target != Ref.TARGET_PARENT) {
                rect = measuredRects.get(target);
                if (rect == null) {
                    logError("view not found: " + debugInfo);
                }
            } else {
                rect = parent;
            }
            switch (property) {
                case PROP_LEFT:
                    if (target == TARGET_PARENT) {
                        return 0;
                    }
                    return rect.left / screenDensity;
                case PROP_RIGHT:
                    if (target == TARGET_PARENT) {
                        return rect.width() / screenDensity;
                    }
                    return rect.right / screenDensity;
                case PROP_TOP:
                    if (target == TARGET_PARENT) {
                        return 0;
                    }
                    return rect.top / screenDensity;
                case PROP_BOTTOM:
                    if (target == TARGET_PARENT) {
                        return rect.height() / screenDensity;
                    }
                    return rect.bottom / screenDensity;
                default:
                    logError("property not found");
            }
            return Float.NaN;
        }
    }

}
