package com.useriq;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;

/**
 * Retrier wraps the given callable & automatically retries it
 * with exponential backoff whenever the given Exception.class is raised.
 * <p>
 * Retrier uses the given executor without creating additional thread.
 *
 * @author sudhakar
 * @created 02-JAN-17
 */
public class Retrier {
    private final Callable callable;
    private final Class<? extends Exception> klass;
    private Backoff policy;
    private boolean cancelRetry;

    public Retrier(Callable callable, Class<? extends Exception> klass) {
        this.callable = callable;
        this.klass = klass;
        this.policy = new Backoff();
    }

    /**
     * Starts the Retrier executing the callable.
     * This method is idempotent, multiple calls will not spawn
     * multiple callables.
     */
    public void start() {
        start(false);
    }

    /**
     * Starts the retry dance
     *
     * @param force - force even if policy is expired
     */
    private synchronized void start(boolean force) {
        while (force || !(policy.isExpired() || cancelRetry)) {
            try {
                callable.call();
            } catch (Exception e) {
                if (klass.isAssignableFrom(e.getClass())) {
                    long duration = policy.duration();
                    System.out.println("sleeping for " + duration + "ms");

                    try {
                        Thread.sleep(duration);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    System.err.println("Non retriyable error");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * reset Resets the duration after successful completion of task
     */
    public void reset() {
        policy.reset();
    }

    void retryOnce() {
        cancel();
        start(true);
    }

    public void cancel() {
        cancelRetry = true;
    }

    private static class Backoff {
        final long ms;
        final long max;
        final int factor;
        final double jitter;
        final int maxTries = 100;

        private int attempts;

        Backoff() {
            max = 60 * 1000;
            ms = 20 * 1000;
            factor = 3;
            jitter = 0.5;
            attempts = 0;
        }

        public Backoff(long ms, long max, int factor, double jitter) {
            this.ms = ms;
            this.max = max;
            this.factor = factor;
            this.jitter = jitter;
            attempts = 0;
        }

        long duration() {
            BigInteger ms = BigInteger.valueOf(this.ms)
                    .multiply(BigInteger.valueOf(this.factor).pow(this.attempts++));
            double rand = Math.random();
            BigInteger deviation = BigDecimal.valueOf(rand)
                    .multiply(BigDecimal.valueOf(jitter))
                    .multiply(new BigDecimal(ms)).toBigInteger();
            ms = (((int) Math.floor(rand * 10)) & 1) == 0 ? ms.subtract(deviation) : ms.add(deviation);
            long delay = ms.min(BigInteger.valueOf(this.max)).longValue();
            delay = delay >= 0 ? delay : 0;
            return delay;
        }

        boolean isExpired() {
            return attempts >= maxTries;
        }

        void reset() {
            this.attempts = 0;
        }
    }
}
