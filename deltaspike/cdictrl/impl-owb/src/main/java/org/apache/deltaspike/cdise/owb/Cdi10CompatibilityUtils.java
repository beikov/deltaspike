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

import java.lang.reflect.Method;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;

public class Cdi10CompatibilityUtils
{

    private static final Class<?> ALTERABLE_CONTEXT_CLASS;
    private static final Method DESTROY_BEAN_METHOD;

    static
    {
        Class<?> alterableContextClazz = null;
        Method destroyBean = null;
        try
        {
            alterableContextClazz = Class.forName("javax.enterprise.context.spi.AlterableContext");
            destroyBean = alterableContextClazz.getMethod("destroy", Contextual.class);
        }
        catch (Exception e)
        {
            // Ignore
        }
        
        ALTERABLE_CONTEXT_CLASS = alterableContextClazz;
        DESTROY_BEAN_METHOD = destroyBean;
    }
    
    private Cdi10CompatibilityUtils ()
    {
        // Prevent instantiation
    }
    
    public static boolean isAlterableContext(Context context)
    {
        return ALTERABLE_CONTEXT_CLASS != null && ALTERABLE_CONTEXT_CLASS.isInstance(context);
    }

    public static void destroyBean(Context context, Contextual<?> contextual)
    {
        try
        {
            DESTROY_BEAN_METHOD.invoke(context, contextual);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Destroying bean failed!", e);
        }
    }

}
