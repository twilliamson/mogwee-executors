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

import java.util.concurrent.Callable;

class WrappedCallable<T> implements Callable<T>
{
    private final Logger log;
    private final Callable<T> callable;

    private WrappedCallable(Logger log, Callable<T> callable)
    {
        this.log = log;
        this.callable = callable;
    }

    public static <T> Callable<T> wrap(Logger log, Callable<T> callable)
    {
        return callable instanceof WrappedCallable ? callable : new WrappedCallable<T>(log, callable);
    }

    @Override
    public T call() throws Exception
    {
        Thread currentThread = Thread.currentThread();

        try {
            return callable.call();
        }
        catch (Exception e) {
            // since callables are expected to sometimes throw exceptions, log this at DEBUG instead of ERROR
            log.debugf(e, "%s ended with an exception", currentThread);

            throw e;
        }
        catch (Error e) {
            log.errorf(e, "%s ended with an exception", currentThread);

            throw e;
        }
        finally {
            log.debugf("%s finished executing", currentThread);
        }
    }
}
