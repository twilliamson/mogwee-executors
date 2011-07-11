/*
 * Copyright 2011 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.mogwee.executors;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Similar to Java's {@link java.util.concurrent.Executors}, but ensures either a {@link LoggingExecutor} or named {@link FailsafeScheduledExecutor} is used.
 */
public class Executors
{
    public static ExecutorService newFixedThreadPool(int nThreads, String name)
    {
        return new LoggingExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(name));
    }

    public static ExecutorService newSingleThreadExecutor(String name)
    {
        return new FinalizableDelegatedExecutorService(new LoggingExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(name)));
    }

    public static ExecutorService newCachedThreadPool(String name)
    {
        return new LoggingExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory(name));
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(String name)
    {
        return new DelegatedScheduledExecutorService(new FailsafeScheduledExecutor(name));
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, String name)
    {
        return new FailsafeScheduledExecutor(corePoolSize, name);
    }

    private static class DelegatedExecutorService extends AbstractExecutorService
    {
        private final ExecutorService e;

        DelegatedExecutorService(ExecutorService executor)
        {
            e = executor;
        }

        @Override
        public void execute(Runnable command)
        {
            e.execute(command);
        }

        @Override
        public void shutdown()
        {
            e.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow()
        {
            return e.shutdownNow();
        }

        @Override
        public boolean isShutdown()
        {
            return e.isShutdown();
        }

        @Override
        public boolean isTerminated()
        {
            return e.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
        {
            return e.awaitTermination(timeout, unit);
        }

        @Override
        public Future<?> submit(Runnable task)
        {
            return e.submit(task);
        }

        @Override
        public <T> Future<T> submit(Callable<T> task)
        {
            return e.submit(task);
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result)
        {
            return e.submit(task, result);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
        {
            return e.invokeAll(tasks);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException
        {
            return e.invokeAll(tasks, timeout, unit);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
        {
            return e.invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
        {
            return e.invokeAny(tasks, timeout, unit);
        }
    }

    private static class FinalizableDelegatedExecutorService extends DelegatedExecutorService
    {
        FinalizableDelegatedExecutorService(ExecutorService executor)
        {
            super(executor);
        }

        @Override
        protected void finalize() throws Throwable
        {
            super.shutdown();
            super.finalize();
        }
    }

    private static class DelegatedScheduledExecutorService extends DelegatedExecutorService implements ScheduledExecutorService
    {
        private final ScheduledExecutorService e;

        DelegatedScheduledExecutorService(ScheduledExecutorService executor)
        {
            super(executor);
            e = executor;
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit)
        {
            return e.schedule(command, delay, unit);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit)
        {
            return e.schedule(callable, delay, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
        {
            return e.scheduleAtFixedRate(command, initialDelay, period, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
        {
            return e.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }
    }

    private Executors()
    {
    }
}
