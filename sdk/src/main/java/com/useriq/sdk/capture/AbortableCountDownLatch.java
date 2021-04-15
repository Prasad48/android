package com.useriq.sdk.capture;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AbortableCountDownLatch extends CountDownLatch {
    private String abortReason = null;

    public AbortableCountDownLatch(int count) {
        super(count);
    }


   /**
     * Unblocks all threads waiting on this latch and cause them to receive an
     * AbortedException.  If the latch has already counted all the way down,
     * this method does nothing.
     */
    public void abort(String reason) {
        if( getCount()==0 )
            return;

        this.abortReason = reason;
        while(getCount()>0)
            countDown();
    }


    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        final boolean rtrn = super.await(timeout,unit);
        if (abortReason != null)
            throw new AbortedException(abortReason);
        return rtrn;
    }

    @Override
    public void await() throws InterruptedException {
        super.await();
        if (abortReason != null)
            throw new AbortedException(abortReason);
    }


    public static class AbortedException extends InterruptedException {
        AbortedException(String reason) {
            super(reason);
        }
    }
}
