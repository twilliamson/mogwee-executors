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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class WrappedRunnableFuture<V> implements Future<V>
{
    private final WrappedRunnable runnable;
    private final Future<V> delegate;

    private WrappedRunnableFuture(WrappedRunnable runnable, Future<V> delegate)
    {
        this.runnable = runnable;
        this.delegate = delegate;
    }

    public static <V> WrappedRunnableFuture wrap(WrappedRunnable runnable, Future<V> delegate)
    {
        return new WrappedRunnableFuture(runnable, delegate);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return delegate.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled()
    {
        return delegate.isCancelled();
    }

    @Override
    public boolean isDone()
    {
        return delegate.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException
    {
        V result = delegate.get();

        checkForException();

        return result;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        V result = delegate.get(timeout, unit);

        checkForException();

        return result;
    }

    private void checkForException() throws InterruptedException, ExecutionException
    {
        Throwable exception = runnable.getException();

        if (exception != null) {
            if (exception instanceof InterruptedException) {
                throw (InterruptedException) exception;
            }

            if (exception instanceof ExecutionException) {
                throw (ExecutionException) exception;
            }

            throw new ExecutionException(exception);
        }
    }
}
