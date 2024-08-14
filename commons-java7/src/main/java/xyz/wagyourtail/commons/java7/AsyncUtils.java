package xyz.wagyourtail.commons.java7;

import org.jetbrains.annotations.NotNull;
import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.function.IOConsumer;
import xyz.wagyourtail.commons.core.function.IOFunction;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class AsyncUtils {
    // basically a fork-join pool similar to the commonPool, since java 7 doesn't include it.
    private static final ForkJoinPool pool = new ForkJoinPool(Math.min(Math.max(1, Runtime.getRuntime().availableProcessors() - 1), Short.MAX_VALUE), ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);

    @SafeVarargs
    public static <T> Future<List<T>> waitForFutures(Future<T>... futures) {
        return waitForFutures(new ArrayDeque<>(Arrays.asList(futures)));
    }

    public static <T> Future<List<T>> waitForFutures(final Queue<Future<T>> futures) {
        final AtomicReference<List<T>> result = new AtomicReference<>();
        return new Future<List<T>>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return result.get() != null;
            }

            @Override
            public List<T> get() throws InterruptedException, ExecutionException {
                try {
                    return get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public List<T> get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                List<T> list = new ArrayList<>();
                long remainingMs = unit.toMillis(timeout);
                while (!futures.isEmpty()) {
                    Future<T> future = futures.poll();
                    long start = System.currentTimeMillis();
                    list.add(future.get(remainingMs, unit));
                    remainingMs -= System.currentTimeMillis() - start;
                }
                result.set(list);
                return list;
            }
        };
    }

    public static <T> Future<Void> forEachAsync(final Collection<T> paths, final IOConsumer<T> fileVisitor) {
        final Queue<Future<Void>> futures = new ArrayDeque<>();
        for (final T path : paths) {
            futures.add(pool.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        fileVisitor.accept(path);
                    } catch (IOException e) {
                        Utils.<RuntimeException>sneakyThrow(e);
                    }
                }
            }, (Void) null));
        }
        return (Future) waitForFutures(futures);
    }

    public static Future<Void> runAll(Runnable... runnables) {
        final Queue<Future<Void>> futures = new ArrayDeque<>();
        for (final Runnable runnable : runnables) {
            futures.add(pool.submit(runnable, (Void) null));
        }
        return (Future) waitForFutures(futures);
    }

    public static Future<Void> visitPathsAsync(final Path start, final IOFunction<Path, Boolean> folderVisitor, final IOConsumer<Path> fileVisitor) throws IOException {
        final Queue<Future<Void>> futures = new ArrayDeque<>();
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, BasicFileAttributes attrs) throws IOException {
                if (folderVisitor != null) {
                    if (!folderVisitor.apply(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }
                if (dir.equals(start)) {
                    return FileVisitResult.CONTINUE;
                }
                futures.add(pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            visitPathsAsync(dir, folderVisitor, fileVisitor).get();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (ExecutionException | IOException e) {
                            Utils.<RuntimeException>sneakyThrow(e);
                        }
                    }
                }, (Void) null));
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, BasicFileAttributes attrs) {
                futures.add(pool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            fileVisitor.accept(file);
                        } catch (IOException e) {
                            Utils.<RuntimeException>sneakyThrow(e);
                        }
                    }
                }, (Void) null));
                return FileVisitResult.CONTINUE;
            }
        });
        return (Future) waitForFutures(futures);
    }

}
