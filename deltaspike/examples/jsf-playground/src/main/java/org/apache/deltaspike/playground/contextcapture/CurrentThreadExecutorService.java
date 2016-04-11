/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.deltaspike.playground.contextcapture;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CurrentThreadExecutorService implements ExecutorService
{
    
    private boolean shutdown;
    private boolean terminated;

    @Override
    public void execute(Runnable command)
    {
        command.run();
    }

    @Override
    public void shutdown()
    {
        shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow()
    {
        shutdown = true;
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown()
    {
        return shutdown;
    }

    @Override
    public boolean isTerminated()
    {
        return terminated;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
    {
        terminated = true;
        return true;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task)
    {
        try
        {
            final T result = task.call();
            return new SimpleFuture<T>(result, null);
        }
        catch (final Exception e)
        {
            return new SimpleFuture<T>(null, e);
        }
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result)
    {
        try
        {
            task.run();
            return new SimpleFuture<T>(result, null);
        }
        catch (final Exception e)
        {
            return new SimpleFuture<T>(null, e);
        }
    }

    @Override
    public Future<?> submit(Runnable task)
    {
        try
        {
            task.run();
            return new SimpleFuture<Object>(null, null);
        }
        catch (final Exception e)
        {
            return new SimpleFuture<Object>(null, e);
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
    {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException
    {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
    {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException
    {
        throw new UnsupportedOperationException("Not implemented!");
    }

}
