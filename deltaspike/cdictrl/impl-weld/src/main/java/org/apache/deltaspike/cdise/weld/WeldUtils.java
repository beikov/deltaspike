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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.enterprise.context.spi.Contextual;

import org.jboss.weld.context.AbstractBoundContext;
import org.jboss.weld.context.AbstractManagedContext;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.context.beanstore.BeanStore;
import org.jboss.weld.context.beanstore.BoundBeanStore;
import org.jboss.weld.context.http.HttpConversationContext;

public class WeldUtils
{

    private static final Method getBeanStore;
    private static final Method setBeanStore;
    private static final Method getBeanId;
    private static final Method getBeanStoreInstance;
    private static final Method removeBeanStoreInstance;
    private static final Method removeState;
    
    static
    {
        try
        {
            Method getBeanStoreMethod = AbstractBoundContext.class.getDeclaredMethod("getBeanStore", new Class[0]);
            getBeanStoreMethod.setAccessible(true);
            getBeanStore = getBeanStoreMethod;
            Method setBeanStoreMethod = AbstractBoundContext.class.getDeclaredMethod("setBeanStore", BoundBeanStore.class);
            setBeanStoreMethod.setAccessible(true);
            setBeanStore = setBeanStoreMethod;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not initialize context capture for Weld!", e);
        }
        
        try
        {
            Method getBeanIdMethod = org.jboss.weld.context.AbstractContext.class.getDeclaredMethod("getId", Contextual.class);
            getBeanIdMethod.setAccessible(true);
            getBeanId = getBeanIdMethod;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not initialize context handling for Weld!", e);
        }

        try
        {
            getBeanStoreInstance = BeanStore.class.getMethod("get", getBeanId.getReturnType());
            removeBeanStoreInstance = BeanStore.class.getMethod("remove", getBeanId.getReturnType());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not initialize context handling for Weld!", e);
        }

        try
        {
            Method removeStateMethod = AbstractManagedContext.class.getDeclaredMethod("removeState");
            removeStateMethod.setAccessible(true);
            removeState = removeStateMethod;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not initialize context handling for Weld!", e);
        }
    }
    
    public static BoundBeanStore getBeanStore(Object context)
    {
        try
        {
            return (BoundBeanStore) getBeanStore.invoke(context);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Retrieving bean store for context capture failed!", e);
        }
    }
    
    public static void setBeanStore(Object context, BoundBeanStore beanStore)
    {
        try
        {
            setBeanStore.invoke(context, beanStore);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Retrieving bean store for context capture failed!", e);
        } 
    }
    
    public static Object getBeanId(Object context)
    {
        try
        {
            return (BoundBeanStore) getBeanId.invoke(context);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Retrieving bean id for context failed!", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static ContextualInstance<Object> getBeanStoreInstance(BoundBeanStore beanStore, Object beanId)
    {
        try
        {
            return (ContextualInstance<Object>) getBeanStoreInstance.invoke(beanStore, beanId);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Retrieving bean store instance for context handling failed!", e);
        }
    }

    public static void removeBeanStoreInstance(BoundBeanStore beanStore, Object beanId)
    {
        try
        {
            removeBeanStoreInstance.invoke(beanStore, beanId);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Removing bean store instance for context handling failed!", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void setInitialized(HttpConversationContext httpConversationContext)
    {
        boolean madeAccessible = false;
        Field initialized = null;
        try
        {
            initialized = httpConversationContext.getClass().getDeclaredField("initialized");
            madeAccessible = !initialized.isAccessible();
            
            if (madeAccessible)
            {
                initialized.setAccessible(true);
            }
            
            ((ThreadLocal<Object>) initialized.get(httpConversationContext)).set(Boolean.TRUE);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Retrieving component instance map for context capture failed!", e);
        }
        finally
        {
            if (madeAccessible)
            {
                initialized.setAccessible(false);
            }
        }
    }

    public static void removeState(HttpConversationContext httpConversationContext) {
        try
        {
            removeState.invoke(httpConversationContext);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Removing state for context handling failed!", e);
        }
    }

}
