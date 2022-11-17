package com.knziha.polymer.wget;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LimitThreadPool extends ThreadPoolExecutor {
    Object lock = new Object();
    int count = 0;

    protected static class BlockUntilFree implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // Access to the task queue is intended primarily for
            // debugging and monitoring. This queue may be in active
            // use.
            //
            // So we are a little bit off road here :) But since we have
            // full control over executor we are safe.
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                // since we cound not rethrow interrupted exception. mark thread
                // as interrupted. and check thread status later in
                // blockExecute()
                Thread.currentThread().interrupt();
            }
        }
    }

    protected static class SafetyCheck implements Runnable {
        Runnable r;

        public SafetyCheck(Runnable r) {
            this.r = r;
        }

        public void run() {
            throw new RuntimeException("should never call run() on this class");
        }

        public Runnable getCause() {
            return r;
        }
    }

    public LimitThreadPool(int maxThreadCount) {
        super(0, maxThreadCount, 0, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new BlockUntilFree());
    }

    protected void beforeExecute(Thread t, Runnable r) {
        synchronized (lock) {
            count++;
        }

        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        synchronized (lock) {
            count--;

            lock.notifyAll();
        }
    }

    /**
     * downloader working if here any getTasks() > 0
     */
    public boolean active() {
        synchronized (lock) {
            return count > 0;
        }
    }

    /**
     * Wait until current task ends. if here is no tasks exit immidiatly.
     * 
     * @throws InterruptedException
     * 
     */
    public void waitUntilNextTaskEnds() throws InterruptedException {
        synchronized (lock) {
            if (active()) {
                lock.wait();
            }
        }
    }

    /**
     * Wait until thread pool execute its last task. Waits forever unti end.
     * 
     * @throws InterruptedException
     * 
     */
    public void waitUntilTermination() throws InterruptedException {
        synchronized (lock) {
            while (active())
                waitUntilNextTaskEnds();
        }
    }

    /**
     * You should not call this method on this Limited Version Thread Pool. Use
     * blockExecute() instead.
     */
    @Override
    public void execute(Runnable command) {
        SafetyCheck s = (SafetyCheck) command;

        super.execute(s.getCause());
    }

    public void blockExecute(Runnable command) throws InterruptedException {
        execute(new SafetyCheck(command));

        if (Thread.interrupted())
            throw new InterruptedException();
    }
}