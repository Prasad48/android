package com.useriq.sdk;

import android.support.annotation.NonNull;

import com.useriq.Logger;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * ThreadExecutor creates singleThreadExecutor with custom threadGroup & exception handler
 */
public class ThreadExecutor {
    private static final Logger logger = Logger.init(ThreadExecutor.class.getSimpleName());
    private static final ThreadGroup threadGroup = new ThreadGroup("UserIQ");
    private final ExecutorService executorService;

    ThreadExecutor(final String label) {
        ThreadFactory tf = new ThreadFactory() {
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = new Thread(threadGroup, r, label);
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler(uncaughtHandler);
                return thread;
            }
        };

        executorService = Executors.newSingleThreadExecutor(tf);
    }

    /**
     * Execute the runnable using a thread pool
     *
     * @param runnable
     */
    public void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    private static final UncaughtExceptionHandler uncaughtHandler = new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            logger.e("Thread.UncaughtException", throwable);
        }
    };
}

