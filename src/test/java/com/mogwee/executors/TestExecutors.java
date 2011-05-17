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

import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class TestExecutors
{
    private void runTest(ExecutorService executorService) throws InterruptedException
    {
        Logger loggingLogger = Logger.getLogger(LoggingExecutor.class);
        Logger failsafeLogger = Logger.getLogger(FailsafeScheduledExecutor.class);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        WriterAppender dummyAppender = new WriterAppender(new SimpleLayout(), bos);

        dummyAppender.setImmediateFlush(true);
        loggingLogger.addAppender(dummyAppender);
        failsafeLogger.addAppender(dummyAppender);
        executorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                throw new RuntimeException("Fail!");
            }
        });
        executorService.shutdown();
        Assert.assertTrue(executorService.awaitTermination(1000, TimeUnit.SECONDS));
        loggingLogger.removeAppender(dummyAppender);
        failsafeLogger.removeAppender(dummyAppender);

        String actual = bos.toString();
        Pattern expected = Pattern.compile("^ERROR - Thread\\[TestLoggingExecutor-[^\\]]+\\] ended abnormally with an exception\njava.lang.RuntimeException: Fail!\n");

        Assert.assertTrue(expected.matcher(actual).find(), String.format("Expected to see:\n%s\nin:\n%s", indent(expected.toString()), indent(actual)));

    }
    @Test(groups = "fast")
    public void testSingleThreadExecutorException() throws InterruptedException
    {
        runTest(Executors.newSingleThreadExecutor("TestLoggingExecutor"));
    }

    @Test(groups = "fast")
    public void testCachedThreadPoolException() throws InterruptedException
    {
        runTest(Executors.newCachedThreadPool("TestLoggingExecutor"));
    }

    @Test(groups = "fast")
    public void testFixedThreadPoolException() throws InterruptedException
    {
        runTest(Executors.newFixedThreadPool(10, "TestLoggingExecutor"));
    }

    @Test(groups = "fast")
    public void testScheduledThreadPoolException() throws InterruptedException
    {
        runTest(Executors.newScheduledThreadPool(10, "TestLoggingExecutor"));
    }

    @Test(groups = "fast")
    public void testSingleThreadScheduledExecutorException() throws InterruptedException
    {
        runTest(Executors.newSingleThreadScheduledExecutor("TestLoggingExecutor"));
    }

    private String indent(String str)
    {
        return "\t" + str.replaceAll("\n", "\n\t");
    }
}
