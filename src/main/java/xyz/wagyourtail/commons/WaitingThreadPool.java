package xyz.wagyourtail.commons;

import java.util.ArrayDeque;
import java.util.function.Consumer;

public class WaitingThreadPool {
    public final int maxFreeThreads;
    private final ArrayDeque<PoolThread> freeThreads = new ArrayDeque<>();
    private final String name;

    public WaitingThreadPool() {
        this(Runtime.getRuntime().availableProcessors() * 3);
    }

    public WaitingThreadPool(int maxFreeThreads) {
        this("Waiting ThreadPool", maxFreeThreads);
    }

    public WaitingThreadPool(String name, int maxFreeThreads) {
        this.name = name;
        this.maxFreeThreads = maxFreeThreads;
        for (int i = 0; i < maxFreeThreads; i++) {
            PoolThread t = new PoolThread();
            t.start();
            freeThreads.addLast(t);
        }
        runTask(() -> {
            Thread.currentThread().setName(name + " Root Thread");
            while (true) {
                synchronized (freeThreads) {
                    try {
                        freeThreads.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (freeThreads.size() < maxFreeThreads) {
                        PoolThread t = new PoolThread();
                        t.start();
                        freeThreads.addLast(t);
                    }
                }
            }
        });
    }

    public Thread runTask(Runnable task) {
        PoolThread t;
        synchronized (freeThreads) {
            if (freeThreads.isEmpty()) {
                // this shouldn't happen, I guess if called too fast...
                t = new PoolThread();
                t.runTask(task);
                t.start();
            } else {
                t = freeThreads.removeLast();
                freeThreads.notify();
                t.runTask(task);
            }
        }
        return t;
    }

    public Thread runTask(Runnable task, Consumer<Thread> beforeRunTask) {
        PoolThread t;
        synchronized (freeThreads) {
            if (freeThreads.isEmpty()) {
                // this shouldn't happen, I guess if called too fast...
                t = new PoolThread();
                beforeRunTask.accept(t);
                t.runTask(task);
                t.start();
            } else {
                t = freeThreads.removeLast();
                freeThreads.notify();
                beforeRunTask.accept(t);
                t.runTask(task);
            }
        }
        return t;
    }

    private class PoolThread extends Thread {
        private Runnable task;

        public PoolThread() {
            super(name + " Thread");
            setDaemon(true);
        }

        public void runTask(Runnable task) {
            synchronized (this) {
                this.setContextClassLoader(Thread.currentThread().getContextClassLoader());
                this.task = task;
                notify();
            }
        }

        @Override
        public void run() {
            try {
                synchronized (this) {
                    while (task == null) {
                        wait();
                    }
                }
                task.run();
            } catch (Throwable ignored) {
            }
        }

    }

}
