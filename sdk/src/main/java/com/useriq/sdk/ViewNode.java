package com.useriq.sdk;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.useriq.sdk.util.Utils;

import static com.useriq.sdk.ViewUtils.getScreenHeight;
import static com.useriq.sdk.ViewUtils.getScreenWidth;
import static com.useriq.sdk.ViewUtils.isAttachedToWindow;

/**
 * ViewNode is a wrapper around {@link android.view.View} with ability to lazily
 * compute siblings & traverse using {@link ViewTree.PreOrderDFSIterator}. Most
 * of the values are computed lazily and cached!
 *
 * @author sudhakar
 * @created 22-Nov-2018
 */
public class ViewNode {
    @SuppressLint("StaticFieldLeak")
    private static final ViewNode UNSET = new ViewNode(null);
    private static final String STR_UNSET = "UNSET";

    // use thread locals for any mutable temp objects
    private static final ThreadLocal<Rect> TMP_RECT = new ThreadLocal<Rect>() {
        protected Rect initialValue() {
            return new Rect();
        }
    };

    private static final Rect SCREEN = new Rect(0, 0,
            getScreenWidth(), getScreenHeight());

    private final View view;
    private int index;
    private ViewNode parent = UNSET;
    private ViewNode firstChild = UNSET;
    private ViewNode leftSibling = UNSET;
    private ViewNode rightSibling = UNSET;

    private String className = null;
    private String testID = STR_UNSET;
    private Rect visibleRect = null;
    private Boolean isVisibleOnScreen = null;
    private Rect clippedRect = null;
    private Rect globalRect = null;
    private ViewGroup rootView = null;

    public ViewNode(View view) {
        this.view = view;

        if (view != null && view.getParent() instanceof ViewGroup) {
            this.index = ((ViewGroup) view.getParent()).indexOfChild(view);
        }
    }

    public View getView() {
        return view;
    }

    public ViewNode getParent() {
        if (parent != UNSET) return parent;

        parent = null;
        ViewParent viewParent = view.getParent();

        if (viewParent instanceof ViewGroup) {
            parent = new ViewNode((View) viewParent);
        }

        return parent;
    }

    public ViewNode getFirstChild() {
        if (firstChild != UNSET) return firstChild;

        firstChild = null;

        if (view instanceof ViewGroup) {
            View childAt0 = ((ViewGroup) view).getChildAt(0);
            if (childAt0 != null) {
                firstChild = new ViewNode(childAt0);
                firstChild.parent = this;
                firstChild.index = 0;
            }
        }

        return firstChild;
    }

    public ViewNode getLeftSibling() {
        if (leftSibling != UNSET) return leftSibling;

        leftSibling = null;
        ViewNode parent = getParent();

        if (parent != null && index > 0 && parent.getView() instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent.getView();
            View sibling = viewGroup.getChildAt(index - 1);
            if (sibling != null) {
                leftSibling = new ViewNode(sibling);
                leftSibling.parent = parent;
                leftSibling.rightSibling = this;
            }
        }

        return leftSibling;
    }

    public ViewNode getRightSibling() {
        if (rightSibling != UNSET) return rightSibling;

        rightSibling = null;
        ViewNode parent = getParent();

        if (parent != null && parent.getView() instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) parent.getView();
            View sibling = viewGroup.getChildAt(index + 1);
            if (sibling != null) {
                rightSibling = new ViewNode(sibling);
                rightSibling.parent = parent;
                rightSibling.leftSibling = this;
            }
        }

        return rightSibling;
    }

    public String getClassName() {
        if (className != null) return className;

        className = view.getClass().getCanonicalName();
        return className;
    }

    @ColorInt
    public int getBgColor() {
        Drawable background = view.getBackground();
        if (!(background instanceof ColorDrawable)) {
            return Color.TRANSPARENT;
        }

        ColorDrawable colorDrawable = (ColorDrawable) background;

        return colorDrawable.getColor();
    }

    public String getTestID() {
        if (!STR_UNSET.equals(testID)) return testID;

        Object tag = view.getTag();
        testID = null;

        if (tag instanceof String) {
            testID = (String) tag;
        } else if (Utils.getRNTestIdKey() != -1) {
            tag = view.getTag(Utils.getRNTestIdKey());
            if (tag instanceof String) {
                testID = (String) tag;
            }
        }

        return testID;
    }

    public boolean isVisible() {
        return isAttachedToWindow(view)
                // Attached to invisible window means this view is not visible.
                && view.getWindowVisibility() == View.VISIBLE
                && view.getAlpha() > 0;
    }

    private boolean isBgDrawn() {
        Drawable drawable = view.getBackground();
        if (drawable == null) return false;
        if (drawable.getOpacity() == PixelFormat.TRANSPARENT) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return drawable.getAlpha() != 0;
        }

        return true;
    }

    /**
     * Computes whether this view is visible to the user. Such a view is
     * attached, visible, all its predecessors are visible, it is not clipped
     * entirely by its predecessors, and has an alpha greater than zero.
     * <p>
     * NOTE: Although evaluated lazily, this traverses up and down the tree to
     * compute the visibility. So use it as the last resort to invalidate the match.
     *
     * @return Whether the view is visible on the screen.
     */
    public boolean isVisibleOnScreen() {
        if (isVisibleOnScreen != null) return isVisibleOnScreen;

        if (!isVisible()) {
            isVisibleOnScreen = false;
            return false;
        }

        // An invisible predecessor or one with alpha zero means
        // that this view is not visible to the user.
        ViewParent current = view.getParent();
        while (current instanceof View) {
            View currView = (View) current;
            // We have attach info so this view is attached and there is no
            // need to check whether we reach to ViewRootImpl on the way up.
            if (currView.getAlpha() <= 0 || currView.getVisibility() != View.VISIBLE) {
                isVisibleOnScreen = false;
                return false;
            }
            current = currView.getParent();
        }

        Rect visibleRect = getVisibleRect2();
        isVisibleOnScreen = !visibleRect.isEmpty();

        return isVisibleOnScreen;
    }

    private ViewGroup getRootView() {
        if (rootView != null) return rootView;

        ViewNode parent = getParent();
        while (parent != null) {
            if (parent.getView() instanceof ViewGroup)
                rootView = (ViewGroup) parent.getView();
            else break;
            parent = parent.getParent();
        }

        return rootView;
    }

    private Rect getGlobalRect() {
        if (globalRect != null) return globalRect;

        globalRect = new Rect();

        if (getView() == null) return globalRect;

        view.getGlobalVisibleRect(globalRect);

        return globalRect;
    }

    private Rect getVisibleRect2() {
        if (visibleRect != null) return visibleRect;

        visibleRect = new Rect();

        if (view instanceof UIRootView) {
            // our view doesn't occupy any space :)
            return visibleRect;
        }

        visibleRect.set(getGlobalRect());

        if (!isVisible()) {
            visibleRect.setEmpty();
            return visibleRect;
        }

        ViewNode parentNode = getParent();
        if (parentNode == null) {
            boolean intersects = visibleRect.intersect(SCREEN);
            if (!intersects) visibleRect.setEmpty();
            return visibleRect;
        }

        Rect tmpRect = new Rect();

        if (view.willNotDraw()) {
            if (!(view instanceof ViewGroup) || getFirstChild() == null) {
                // no children & not visible
                visibleRect.setEmpty();
                return visibleRect;

            }
            // if we're not drawn, lets narrow down to actual drawingRect
            // based on union of descendant's visibleReact
            ViewNode child = getFirstChild();

            tmpRect.setEmpty();
            while (child != null) {
                Rect childRect = child.getGlobalRect();
                tmpRect.union(childRect);
                child = child.getRightSibling();
            }
            boolean intersects = visibleRect.intersect(tmpRect);
        } else if (view instanceof ViewGroup && ((ViewGroup) view).getChildCount() == 0 && !isBgDrawn()) {
            visibleRect.setEmpty();
            return visibleRect;
        }

        // Eliminate clipping by younger siblings
        ViewNode sibling = getRightSibling();

        if (sibling != null && sibling != this) {
            tmpRect.setEmpty();
            while (sibling != null) {
                Rect sRect = sibling.getVisibleRect2();
                if (Rect.intersects(visibleRect, sRect)) {
                    tmpRect.union(sRect);
                }
                sibling = sibling.getRightSibling();
            }


            subtract(visibleRect, tmpRect);
        }

        while (parentNode != null) {
            Rect parentRect = parentNode.getVisibleRect2();

            if (!visibleRect.intersect(parentRect)) {
                visibleRect.setEmpty();
                return visibleRect;
            }

            parentNode = parentNode.getParent();
        }

        return visibleRect;
    }

    private static void subtract(Rect a, Rect b) {
        if (!Rect.intersects(a, b)) {
            // if there's no collision, then "a" is not modified!
            return;
        }

        if (b.contains(a)) {
            a.setEmpty();
            return;
        }

        if (a.contains(b)) {
            int leftArea = a.height() * (b.left - a.left);
            int topArea = a.width() * (b.top - a.top);
            int rightArea = a.height() * (a.right - b.right);
            int bottomArea = a.width() * (a.bottom - b.bottom);

            if (rightArea > leftArea && rightArea > topArea && rightArea > bottomArea) {
                a.left = b.right;
            } else if (bottomArea > leftArea && bottomArea > topArea && bottomArea > rightArea) {
                a.top = b.bottom;
            } else if (leftArea > topArea && leftArea > rightArea && leftArea > bottomArea) {
                a.right = b.left;
            } else {
                a.bottom = b.top;
            }

            if (a.left >= a.right || a.top >= a.bottom) a.setEmpty();
        } else {
            if (b.height() * b.width() > a.height() * a.width()) {
                if (a.left < b.left) {
                    if (a.top < b.top) {
                        if (a.width() * (b.top - a.top) > a.height() * (b.left - a.left)) {
                            a.bottom = b.top;
                            return;
                        } else {
                            a.right = b.left;
                        }
                    } else if (a.bottom > b.bottom) {
                        if (a.width() * (a.bottom - b.bottom) > a.height() * (b.left - a.left)) {
                            a.top = b.bottom;
                            return;
                        } else {
                            a.right = b.left;
                        }
                        return;
                    }
                    a.right = b.left;
                }

                if (a.right > b.right) {
                    if (a.top < b.top) {
                        if (a.width() * (b.top - a.top) > a.height() * (a.right - b.right)) {
                            a.bottom = b.top;
                            return;
                        } else {
                            a.left = b.right;
                        }
                    } else if (a.bottom > b.bottom) {
                        if (a.width() * (a.bottom - b.bottom) > a.height() * (a.right - b.right)) {
                            a.top = b.bottom;
                            return;
                        } else {
                            a.left = b.right;
                        }
                        return;
                    }
                    a.left = b.right;
                }

                if (a.top < b.top) {
                    a.bottom = b.top;
                }

                if (a.bottom > b.bottom) {
                    a.top = b.bottom;
                }
            } else {
                int topArea = 0;
                int rightArea = 0;
                int bottomArea = 0;
                int leftArea = 0;
                if (b.top > a.top) {
                    topArea = (b.top - a.top) * a.width();
                }
                if (b.left > a.left) {
                    leftArea = (b.left - a.left) * a.height();
                }
                if (b.right < a.right) {
                    rightArea = (a.right - b.right) * a.height();
                }
                if (b.bottom < a.bottom) {
                    bottomArea = (a.bottom - b.bottom) * a.width();
                }
                int max = Math.max(Math.max(leftArea, rightArea), Math.max(topArea, bottomArea));
                if (max == leftArea) {
                    a.right = b.left;
                } else if (max == rightArea) {
                    a.left = b.right;
                } else if (max == bottomArea) {
                    a.top = b.bottom;
                } else if (max == topArea) {
                    a.bottom = b.top;
                }
            }
        }
    }
}
