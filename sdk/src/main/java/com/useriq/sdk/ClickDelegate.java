package com.useriq.sdk;

import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.useriq.sdk.models.Element;

/**
 * @author sudhakar
 * @created 04-Oct-2018
 */
class ClickDelegate extends View.AccessibilityDelegate {
    private ElementTracker.Callback elTrackerCb;

    final Element element;

    private final View.AccessibilityDelegate oldDelegate;
    private final View.OnAttachStateChangeListener detachListener;

    ClickDelegate(Element element, View.AccessibilityDelegate oldDelegate, ElementTracker.Callback cb) {
        this.elTrackerCb = cb;
        this.element = element;
        this.oldDelegate = oldDelegate;

        this.detachListener = new View.OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
                elTrackerCb.onViewAttached(v, ClickDelegate.this.element);
            }

            public void onViewDetachedFromWindow(View v) {
                v.setAccessibilityDelegate(null);
                v.removeOnAttachStateChangeListener(this);
                elTrackerCb.onViewDetached(v, ClickDelegate.this.element);
            }
        };
    }

    void reset(View view) {
        view.setAccessibilityDelegate(oldDelegate);
        view.removeOnAttachStateChangeListener(detachListener);
    }

    View.AccessibilityDelegate getOldDelegate() {
        return oldDelegate;
    }

    @Override
    public void sendAccessibilityEvent(View host, int eventType) {
        super.sendAccessibilityEvent(host, eventType);

        if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            elTrackerCb.onViewClick(host, element);
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            elTrackerCb.onViewLongClick(host, element);
        } else if (eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            elTrackerCb.onViewFocused(host, element);
        }

        if (oldDelegate != null) {
            oldDelegate.sendAccessibilityEvent(host, eventType);
        }
    }
}
