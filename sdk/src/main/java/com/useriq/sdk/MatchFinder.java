package com.useriq.sdk;

import android.app.Activity;
import android.view.View;

import com.useriq.Logger;
import com.useriq.sdk.capture.ViewRoot;
import com.useriq.sdk.models.Element;
import com.useriq.sdk.models.Screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author sudhakar
 * @created 05-Oct-2018
 */
public class MatchFinder {
    private static final Logger logger = Logger.init(MatchFinder.class.getSimpleName());

    public static Result match(Activity activity, List<Screen> screens) {
        List<ViewRoot> viewRoots;

        if(activity == null) return null;

        try {
            viewRoots = ViewRoot.from(activity);
        } catch (Exception e) {
            logger.e("match(): finding root views failed", e);
            return null;
        }

        for (int i = 0; i < viewRoots.size(); i++) {
            // wIndex is zIndex reversed. So view on top is given 0, and
            // first view at index=0 is given n-1
            int wIndex = viewRoots.size() - i - 1;
            Result result = match(wIndex, viewRoots.get(i).view, screens);
            if (result != null ) return result;
        }

        return null;
    }

    public static View findView(Activity activity, Element element) {
        List<ViewRoot> viewRoots;

        if(activity == null) {
            return null;
        }

        // See if we are already tracking it!
        View target = ElementTracker.getInstance().getView(element);

        if (target != null && target.getContext() == activity) {
            return target;
        }

        try {
            viewRoots = ViewRoot.from(activity);
        } catch (Exception e) {
            logger.e("findView(): finding root views failed", e);
            return null;
        }


        for (int i = 0; i < viewRoots.size(); i++) {
            // wIndex is zIndex reversed. So view on top is given 0, and
            // first view at index=0 is given n-1
            int wIndex = viewRoots.size() - i - 1;
            View view = viewRoots.get(i).view;
            ViewTree viewTree = new ViewTree(view);

            for (ViewNode viewNode: viewTree.preOrderDFSIterator()) {
                if (element.matches(wIndex, viewNode)) {
                    return viewNode.getView();
                }
            }
        }

        return null;
    }

    public static View findView(Activity activity, Element element, int wIndex) {
        List<ViewRoot> viewRoots;

        if(activity == null) {
            return null;
        }

        // See if we are already tracking it!
        View target = ElementTracker.getInstance().getView(element);

        if (target != null && target.getContext() == activity) {
            return target;
        }

        try {
            viewRoots = ViewRoot.from(activity);
        } catch (Exception e) {
            logger.e("findView(): finding root views failed", e);
            return null;
        }

        int idx = viewRoots.size() - wIndex - 1;

        if (idx >= 0 && idx < viewRoots.size()) {
            View view = viewRoots.get(idx).view;

            ViewTree viewTree = new ViewTree(view);

            for (ViewNode viewNode: viewTree.preOrderDFSIterator()) {
                if (element.matches(wIndex, viewNode)) {
                    return viewNode.getView();
                }
            }
        }

        return null;
    }

    private static Result match(int wIndex, View view, List<Screen> screens) {
        ViewTree viewTree = new ViewTree(view);

        Map<Element, ViewNode> matched = new HashMap<>();
        Screen screen = findScreen(screens, wIndex, viewTree, matched);

        if (screen == null) return null;

        for (Element el: screen.elements) {
            if (matched.containsKey(el)) continue;

            for (ViewNode viewNode: viewTree.preOrderDFSIterator()) {
                if (matched.containsValue(viewNode)) continue;

                if (el.matches(wIndex, viewNode)) {
                    matched.put(el, viewNode);
                    if (matched.keySet().containsAll(screen.elements)) {
                        // micro optimization. We break the inner loop
                        break;
                    }
                }
            }
        }

        Result result = new Result(screen);
        for (Map.Entry<Element, ViewNode> entry: matched.entrySet()) {
            result.viewMap.put(entry.getValue().getView(), entry.getKey());
        }

        return result;
    }

    private static Screen findScreen(List<Screen> screens, int wIndex, ViewTree viewTree, Map<Element, ViewNode> matched) {
        for (Screen screen : screens) {
            matched.clear();
            boolean found = match(screen, wIndex, viewTree, matched);
            if (found) return screen;
        }

        return null;
    }

    public static boolean match(Screen screen, int wIndex, ViewTree viewTree, Map<Element, ViewNode> matched) {
        for (Element el: screen.predicateEls) {
            for (ViewNode viewNode: viewTree.preOrderDFSIterator()) {
                if (!matched.containsKey(el) && el.matches(wIndex, viewNode)) {
                    matched.put(el, viewNode);
                    boolean found = matched.keySet().containsAll(screen.predicateEls);
                    if (found) return true;
                }
            }
        }
        return false;
    }

    static class Result {
        final Screen screen;
        final WeakHashMap<View, Element> viewMap = new WeakHashMap<>();

        Result(Screen screen) {
            this.screen = screen;
        }
    }
}
