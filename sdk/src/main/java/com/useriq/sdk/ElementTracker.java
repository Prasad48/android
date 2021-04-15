package com.useriq.sdk;

import android.view.View;

import com.useriq.sdk.capture.Reflect;
import com.useriq.sdk.models.Element;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ElementTracker allows tracking of elements from different sources
 * <p>
 * FIXME: CopyOnWriteArrayList is used to avoid ConcurrentModificationException
 * FIXME: Synchronize properly & profile for performance
 *
 * @author sudhakar
 * @created 11-Nov-2018
 */
public class ElementTracker {
    private static ElementTracker instance;

    private final List<Trackable> trackables = new CopyOnWriteArrayList<>();

    public static ElementTracker getInstance() {
        if (instance == null) instance = new ElementTracker();
        return instance;
    }

    private ElementTracker() {
    }

    public void track(Element element, View view, Callback cb) {
        Trackable oldTrackable = findTrackable(element);

        // Not tracking react native elements, react events are taken by React native's Event Dispatcher
        String canonicalName = view.getClass().getCanonicalName();
        if (canonicalName != null && canonicalName.startsWith("com.facebook.react.views")) return;

        if (oldTrackable == null) {
            Trackable trackable = new Trackable(view, element);
            trackable.addCallback(cb);
            trackables.add(trackable);
        } else {
            oldTrackable.update(view, element);
            if (!oldTrackable.callbacks.contains(cb)) {
                oldTrackable.addCallback(cb);
            }
        }
    }

    public void unTrack(Element element, Callback cb) {
        Trackable oldTrackable = findTrackable(element);

        if (oldTrackable != null) {
            oldTrackable.removeCallback(cb);
        }
    }

    private Trackable findTrackable(Element element) {
        for (Trackable trackable : trackables) {
            if (trackable.delegate.element == element) {
                return trackable;
            }
        }
        return null;
    }

    void reset() {
        for (Trackable trackable : trackables) {
            trackable.reset();
        }
        trackables.clear();
    }

    private void removeTrackable(Trackable trackable) {
        trackables.remove(trackable);
    }

    View getView(Element element) {
        for (Trackable trackable : trackables) {
            if (trackable.delegate.element == element) {
                return trackable.viewRef.get();
            }
        }
        return null;
    }

    private static class Trackable extends Callback {
        private WeakReference<View> viewRef;
        private ClickDelegate delegate;
        private final List<Callback> callbacks = new CopyOnWriteArrayList<>();

        Trackable(View view, Element element) {
            update(view, element);
        }

        void update(View view, Element element) {
            if (viewRef != null && viewRef.get() != null && delegate != null) {
                delegate.reset(viewRef.get());
            }

            this.viewRef = new WeakReference<>(view);

            View.AccessibilityDelegate oldDelegate = Reflect.getAccessibilityDelegate(view);

            if (oldDelegate instanceof ClickDelegate) {
                oldDelegate = ((ClickDelegate) oldDelegate).getOldDelegate();
            }

            this.delegate = new ClickDelegate(element, oldDelegate, this);
            view.setAccessibilityDelegate(delegate);
        }

        public void onViewAttached(View view, Element element) {
            for (Callback cb : callbacks) {
                cb.onViewAttached(view, element);
            }
        }

        public void onViewDetached(View view, Element element) {
            for (Callback cb : callbacks) {
                cb.onViewDetached(view, element);
            }
            this.reset();
            getInstance().removeTrackable(this);
        }

        public void onViewClick(View view, Element element) {
            for (Callback cb : callbacks) {
                cb.onViewClick(view, element);
            }
        }

        public void onViewLongClick(View view, Element element) {
            for (Callback cb : callbacks) {
                cb.onViewLongClick(view, element);
            }
        }

        void reset() {
            View view = viewRef.get();
            if (view != null) delegate.reset(view);
            callbacks.clear();
        }

        void addCallback(Callback cb) {
            callbacks.add(cb);
        }

        void removeCallback(Callback cb) {
            callbacks.remove(cb);
        }
    }

    public static class Callback {
        public void onViewAttached(View view, Element element) {}
        public void onViewDetached(View view, Element element) {}
        public void onViewClick(View view, Element element) {}
        public void onViewLongClick(View view, Element element) {}
        public void onViewFocused(View host, Element element) { }
    }
}
