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
package org.apache.deltaspike.cdise.weld;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.jboss.weld.context.beanstore.BoundBeanStore;

public class DelegatingSynchronizingBoundBeanStore implements InvocationHandler
{

    private final BoundBeanStore delegate;
    private final Object lock;

    public DelegatingSynchronizingBoundBeanStore(BoundBeanStore delegate)
    {
        this.delegate = delegate;
        this.lock = new Object();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        final String methodName = method.getName();
        if (args == null || args.length == 0)
        {
            if ("detach".equals(methodName))
            {
                // true if the bean store was detached
                return true;
            }
            else if ("attach".equals(methodName))
            {
                // false if the bean store is already attached
                return false;
            }
            else if ("toString".equals(methodName))
            {
                return this.toString();
            }
            else if ("hashCode".equals(methodName))
            {
                return this.hashCode();
            }
        }
        else if (args != null && args.length == 1 && "equals".equals(methodName))
        {
            return this == args[0];
        }
        
        synchronized (lock) {
            return method.invoke(delegate, args);
        }
    }
}
