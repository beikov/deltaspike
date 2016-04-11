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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.provider.BeanProvider;

@Named
@RequestScoped
public class ContextCaptureController
{
    
    private boolean loaded;
    
    private long loadTime;
    private String data1;
    private String data2;
    private String data3;
    private String data4;
    private String data5;
    private String data6;
    private String data7;
    private String data8;
    
    @Inject
    private ContextCaptureProducerBean producerBean;
    @Inject
    private ContextControl contextControl;
    @Inject
    private BeanManager beanManager;

    public void loadSynchronously()
    {
        load(new CurrentThreadExecutorService());
    }

    public void loadAsynchronously()
    {
        ExecutorService executorService = Executors.newCachedThreadPool();
        try
        {
            load(executorService);
        }
        finally
        {
            executorService.shutdown();
        }
    }
    
    private void load(ExecutorService executorService)
    {
        // Remove beans
        contextControl.removeFromContext("data1");
        contextControl.removeFromContext("data2");
        contextControl.removeFromContext("data3");
        contextControl.removeFromContext("data4");
        contextControl.removeFromContext("data5");
        contextControl.removeFromContext("data6");
        contextControl.removeFromContext("data7");
        contextControl.removeFromContext("data8");
        
        // Generate data for the test
        String expectedData1 = UUID.randomUUID().toString();
        String expectedData2 = UUID.randomUUID().toString();
        String expectedData3 = UUID.randomUUID().toString();
        String expectedData4 = UUID.randomUUID().toString();
        String expectedData5 = UUID.randomUUID().toString();
        String expectedData6 = UUID.randomUUID().toString();
        String expectedData7 = UUID.randomUUID().toString();
        String expectedData8 = UUID.randomUUID().toString();
        
        // Prepare data for producers
        producerBean.setData1(expectedData1);
        producerBean.setData2(expectedData2);
        producerBean.setData3(expectedData3);
        producerBean.setData4(expectedData4);
        
        // Produce beans that should be captured
        getBeanData("data1");
        getBeanData("data2");
        getBeanData("data3");
        getBeanData("data4");
        
        final long start = System.nanoTime();
        
        // Submit work
        Future<String> f1 = submitSleepingStringHolderResolver(executorService, "data1", TimeUnit.SECONDS.toMillis(2));
        Future<String> f2 = submitSleepingStringHolderResolver(executorService, "data2", TimeUnit.SECONDS.toMillis(2));
        Future<String> f3 = submitSleepingStringHolderResolver(executorService, "data3", TimeUnit.SECONDS.toMillis(2));
        Future<String> f4 = submitSleepingStringHolderResolver(executorService, "data4", TimeUnit.SECONDS.toMillis(2));
        
        List<Future<String>> futures = new ArrayList<Future<String>>();
        
        int producerConcurrency = 4;
        if (producerConcurrency > 1)
        {
            Map<String, Entry<ThreadLocal<String>, String>> data = 
                    new HashMap<String, Entry<ThreadLocal<String>, String>>();
            data.put("data5", new AbstractMap.SimpleEntry<ThreadLocal<String>, String>(producerBean.getData5Holder(),
                    expectedData5));
            data.put("data6", new AbstractMap.SimpleEntry<ThreadLocal<String>, String>(producerBean.getData6Holder(),
                    expectedData6));
            data.put("data7", new AbstractMap.SimpleEntry<ThreadLocal<String>, String>(producerBean.getData7Holder(),
                    expectedData7));
            data.put("data8", new AbstractMap.SimpleEntry<ThreadLocal<String>, String>(producerBean.getData8Holder(),
                    expectedData8));

            for (int i = 0; i < producerConcurrency; i++)
            {
                futures.add(submitSleepingStringHolderConcurrentProducer(executorService, data,
                        TimeUnit.SECONDS.toMillis(2)));
            }
        }
        else
        {
            Future<String> f5 = submitSleepingStringHolderProducer(executorService, "data5",
                    producerBean.getData5Holder(), expectedData5, TimeUnit.SECONDS.toMillis(2));
            Future<String> f6 = submitSleepingStringHolderProducer(executorService, "data6",
                    producerBean.getData6Holder(), expectedData6, TimeUnit.SECONDS.toMillis(2));
            Future<String> f7 = submitSleepingStringHolderProducer(executorService, "data7",
                    producerBean.getData7Holder(), expectedData7, TimeUnit.SECONDS.toMillis(2));
            Future<String> f8 = submitSleepingStringHolderProducer(executorService, "data8",
                    producerBean.getData8Holder(), expectedData8, TimeUnit.SECONDS.toMillis(2));
            
            futures.add(f5);
            futures.add(f6);
            futures.add(f7);
            futures.add(f8);
        }
        
        // Wait for work to be done
        try
        {
            data1 = expectedData1.equals(f1.get()) ? "Success" : "Failed";
            data2 = expectedData2.equals(f2.get()) ? "Success" : "Failed";
            data3 = expectedData3.equals(f3.get()) ? "Success" : "Failed";
            data4 = expectedData4.equals(f4.get()) ? "Success" : "Failed";
            
            for (Future<String> f : futures)
            {
                f.get();
            }
            
            data5 = expectedData5.equals(getBeanData("data5")) ? "Success" : "Failed";
            data6 = expectedData6.equals(getBeanData("data6")) ? "Success" : "Failed";
            data7 = expectedData7.equals(getBeanData("data7")) ? "Success" : "Failed";
            data8 = expectedData8.equals(getBeanData("data8")) ? "Success" : "Failed";
            loaded = true;
            loadTime = (long) (((double) (System.nanoTime() - start)) / 1000000d);

            producerBean.clearData1();
            producerBean.clearData2();
            producerBean.clearData3();
            producerBean.clearData4();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private String getBeanData(String beanName)
    {
        return BeanProvider.getContextualReference(beanManager, beanName, false, StringHolder.class).get();
    }
    
    private Future<String> submitSleepingStringHolderProducer(ExecutorService executorService, String beanName,
            final ThreadLocal<String> holder, final String value, long sleepMillis)
    {
        final Callable<String> resolver = getSubmitSleepingStringHolderResolver(executorService, beanName, sleepMillis);
        Callable<String> callable = new Callable<String>()
        {
            
            @Override
            public String call() throws Exception
            {
                try
                {
                    holder.set(value);
                    return resolver.call();
                }
                finally
                {
                    holder.remove();
                }
            }
        };
        return executorService.submit(callable);
    }
    
    private Future<String> submitSleepingStringHolderConcurrentProducer(ExecutorService executorService,
            final Map<String, Entry<ThreadLocal<String>, String>> data, final long sleepMillis)
    {
        final Map<String, Callable<String>> producers = new HashMap<String, Callable<String>>();
        for (Entry<String, Entry<ThreadLocal<String>, String>> dataEntry : data.entrySet())
        {
            producers.put(dataEntry.getKey(), new SleepingStringHolderResolver(beanManager, dataEntry.getKey(), 0));
        }
        
        Callable<String> callable = new Callable<String>()
        {
            
            @Override
            public String call() throws Exception
            {
                try
                {
                    Thread.sleep(sleepMillis);
                    for (Entry<String, Entry<ThreadLocal<String>, String>> dataEntry : data.entrySet())
                    {
                        dataEntry.getValue().getKey().set(dataEntry.getValue().getValue());
                    }
                    for (Entry<String, Callable<String>> entry : producers.entrySet())
                    {
                        String expectedData = data.get(entry.getKey()).getValue();
                        String actualData = entry.getValue().call();
                        
                        if (!expectedData.equals(actualData))
                        {
                            throw new AssertionError("Excpected: '" + expectedData + "' but was: '" + actualData + "'");
                        }
                    }
                    
                    return "";
                }
                finally
                {
                    for (Entry<String, Entry<ThreadLocal<String>, String>> dataEntry : data.entrySet())
                    {
                        dataEntry.getValue().getKey().remove();
                    }
                }
            }
        };
        if (!(executorService instanceof CurrentThreadExecutorService))
        {
            callable = new ContextCaptureCallable<String>(contextControl, callable);
        }
        return executorService.submit(callable);
    }
    
    private Future<String> submitSleepingStringHolderResolver(ExecutorService executorService, String beanName,
            long sleepMillis)
    {
        Callable<String> callable = getSubmitSleepingStringHolderResolver(executorService, beanName, sleepMillis);
        return executorService.submit(callable);
    }
    
    private Callable<String> getSubmitSleepingStringHolderResolver(ExecutorService executorService, String beanName,
            long sleepMillis)
    {
        Callable<String> callable = new SleepingStringHolderResolver(beanManager, beanName, sleepMillis);
        if (!(executorService instanceof CurrentThreadExecutorService))
        {
            callable = new ContextCaptureCallable<String>(contextControl, callable);
        }
        
        return callable;
    }

    public boolean isLoaded()
    {
        return loaded;
    }

    public long getLoadTime()
    {
        return loadTime;
    }

    public String getData1()
    {
        return data1;
    }

    public String getData2()
    {
        return data2;
    }

    public String getData3()
    {
        return data3;
    }

    public String getData4()
    {
        return data4;
    }

    public String getData5()
    {
        return data5;
    }

    public String getData6()
    {
        return data6;
    }

    public String getData7()
    {
        return data7;
    }

    public String getData8()
    {
        return data8;
    }
}
