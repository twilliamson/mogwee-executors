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

import com.mogwee.logging.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Extension of {@link java.util.concurrent.ThreadPoolExecutor} that ensures any uncaught exceptions are logged.
 */
public class LoggingExecutor extends ThreadPoolExecutor
{
    private static final Logger LOG = Logger.getLogger();

    public LoggingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public LoggingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public LoggingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public LoggingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task)
    {
        return super.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result)
    {
        return super.submit(wrapRunnable(task), result);
    }

    @Override
    public Future<?> submit(Runnable task)
    {
        return super.submit(wrapRunnable(task));
    }

    @Override
    public void execute(Runnable command)
    {
        super.execute(wrapRunnable(command));
    }

    private Runnable wrapRunnable(final Runnable runnable)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                Thread currentThread = Thread.currentThread();

                try {
                    runnable.run();
                }
                catch (RuntimeException e) {
                    LOG.errorf(e, "%s ended abnormally with an exception", currentThread);

                    throw e;
                }
                catch (Error e) {
                    LOG.errorf(e, "%s ended abnormally with an exception", currentThread);

                    throw e;
                }

                LOG.debugf("%s finished executing (%s interrupted)", currentThread, currentThread.isInterrupted() ? "was" : "was not");
            }
        };
    }
}
