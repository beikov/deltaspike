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
package org.apache.deltaspike.cdise.owb;

import java.lang.reflect.Field;
import java.util.Map;

import javax.enterprise.context.spi.Contextual;

import org.apache.webbeans.context.AbstractContext;
import org.apache.webbeans.context.creational.BeanInstanceBag;
import org.apache.webbeans.intercept.ApplicationScopedBeanInterceptorHandler;

public class OwbUtils
{
    
    private OwbUtils ()
    {
        // Prevent instantiation
    }
    
    @SuppressWarnings("unchecked")
    public static Map<Contextual<?>, BeanInstanceBag<?>> getComponentInstanceMap(Object context)
    {
        boolean madeAccessible = false;
        Field componentInstanceMap = null;
        try
        {
            componentInstanceMap = AbstractContext.class.getDeclaredField("componentInstanceMap");
            madeAccessible = !componentInstanceMap.isAccessible();
            
            if (madeAccessible)
            {
                componentInstanceMap.setAccessible(true);
            }
            
            return (Map<Contextual<?>, BeanInstanceBag<?>>) componentInstanceMap.get(context);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Retrieving component instance map for context capture failed!", e);
        }
        finally
        {
            if (madeAccessible)
            {
                componentInstanceMap.setAccessible(false);
            }
        }
    }
    
    public static void setComponentInstanceMap(Object context, Object map)
    {
        boolean madeAccessible = false;
        Field componentInstanceMap = null;
        try
        {
            componentInstanceMap = AbstractContext.class.getDeclaredField("componentInstanceMap");
            madeAccessible = !componentInstanceMap.isAccessible();
            
            if (madeAccessible)
            {
                componentInstanceMap.setAccessible(true);
            }
            
            componentInstanceMap.set(context, map);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Retrieving component instance map for context capture failed!", e);
        }
        finally
        {
            if (madeAccessible)
            {
                componentInstanceMap.setAccessible(false);
            }
        }
    }

    public static Object getOwbContextualInstanceProvider(Object proxy)
    {
        Field field = null;
        boolean madeAccessible = false;
        
        try
        {
            field = proxy.getClass().getDeclaredField("owbContextualInstanceProvider");
            madeAccessible = !field.isAccessible();
            if (madeAccessible)
            {
                field.setAccessible(true);
            }
            
            return field.get(proxy);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Retrieving contextual instance provider for context handling failed!", e);
        }
        finally 
        {
            if (madeAccessible)
            {
                field.setAccessible(false);
            }
        }
    }

    public static void clearCachedInstance(ApplicationScopedBeanInterceptorHandler contextualInstanceProvider)
    {
        Field field = null;
        boolean madeAccessible = false;
        
        try
        {
            field = ApplicationScopedBeanInterceptorHandler.class.getDeclaredField("cachedInstance");
            madeAccessible = !field.isAccessible();
            if (madeAccessible)
            {
                field.setAccessible(true);
            }
            
            field.set(contextualInstanceProvider, null);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Clearing application scoped cached instance for context handling failed!", e);
        }
        finally 
        {
            if (madeAccessible)
            {
                field.setAccessible(false);
            }
        }
    }

}
