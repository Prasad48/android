package com.useriq.sdk;

import android.os.Handler;
import android.os.Message;

/**
 * Throttler invokes runnable using interval as follows
 * 1. `Atleast` ms elapsed on each attempt, unless
 * 2. `Atmost` ms has elapsed since last Fired
 *
 * ie fires after "atleast ms" but no less than "atmost ms" for
 * any previous attempts. Consider the following example
 *
 * let atleast = 300
 * let atmost = 500
 *
 * if events are sparse & with more than 300ms between attempts, then
 * every attempt is made after 300ms
 *
 * if more events comes in before 300ms, delay is increased by 300ms or remaining
 * in atmost whichever is smaller
 *
 * @author sudhakar
 * @created 03-Oct-2018
 */
class Throttler {
    private final Handler mHandler;
    private final long mAtleast;
    private final long mAtmost;

    private long mLastAttempt;
    private long mLastFired;

    Throttler(long atleast, long atmost, final Callback cb) {
        mAtleast = atleast;
        mAtmost = atmost;
        mHandler = new Handler(new Handler.Callback() {
            public boolean handleMessage(Message msg) {
                cb.invoke((String) msg.obj);
                mLastFired = System.currentTimeMillis();
                return true;
            }
        });

        mLastAttempt = mLastFired = System.currentTimeMillis();
    }

    void attempt(String who) {
        long elapsed = mLastAttempt - mLastFired;
        long mostDelay = mAtmost - elapsed;

        mHandler.removeCallbacksAndMessages(null);

        Message msg = Message.obtain();
        msg.obj = who;

        if (mostDelay >= 0) {
            long min = Math.min(mostDelay, mAtleast);
            mHandler.sendMessageDelayed(msg, min);
        } else {
            mHandler.sendMessage(msg);
        }

        mLastAttempt = System.currentTimeMillis();
    }

    void attemptNow(String who) {
        mHandler.removeCallbacksAndMessages(null);

        Message msg = Message.obtain();
        msg.obj = who;
        mHandler.sendMessage(msg);
    }

    void reset() {
        mHandler.removeCallbacksAndMessages(null);
    }

    interface Callback {
        void invoke(String who);
    }
}
