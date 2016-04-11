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

import java.util.concurrent.Callable;

import javax.enterprise.inject.spi.BeanManager;

import org.apache.deltaspike.core.api.provider.BeanProvider;

public class SleepingStringHolderResolver implements Callable<String>
{

    private final BeanManager beanManager;
    private final String beanName;
    private final long sleepMillis;
    
    public SleepingStringHolderResolver(BeanManager beanManager, String beanName, long sleepMillis)
    {
        this.beanManager = beanManager;
        this.beanName = beanName;
        this.sleepMillis = sleepMillis;
    }

    @Override
    public String call() throws Exception
    {
        if (sleepMillis > 0)
        {
            Thread.sleep(sleepMillis);
        }
        return BeanProvider.getContextualReference(beanManager, beanName, false, StringHolder.class).get();
    }
}
