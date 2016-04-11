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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.deltaspike.cdise.api.ContextReference;
import org.jboss.weld.context.beanstore.BoundBeanStore;
import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpSessionContext;

/**
 * Weld specific impl for {@link org.apache.deltaspike.cdise.api.ContextControl#captureContexts()}
 */
@Dependent
public class WeldContextCapturer
{
    
    @Inject
    HttpSessionContext httpSessionContext;
    @Inject
    HttpRequestContext httpRequestContext;
    @Inject
    HttpConversationContext httpConversationContext;
    @Inject
    private HttpServletRequestHolder requestHolder;
    
    private static final Method requestInitializedMethod;
    private static final Method clearMethod;
    
    static
    {
        Method requestInitialized = null;
        Method clear = null;
        
        try
        {
            // SessionHolder is introduced in Weld 2
            Class<?> sessionHolderClass = Class.forName("org.jboss.weld.servlet.SessionHolder");
            requestInitialized = sessionHolderClass.getMethod("requestInitialized", javax.servlet.http.HttpServletRequest.class);
            clear = sessionHolderClass.getMethod("clear");
        }
        catch (Exception e)
        {
            // TODO: Don't throw an exception but do something that is Weld 1 compatible
            throw new RuntimeException("Could not initialize context capture for Weld!", e);
        }
        
        requestInitializedMethod = requestInitialized;
        clearMethod = clear;
    }

    public List<ContextReference> captureContexts()
    {
        List<ContextReference> l = new ArrayList<ContextReference>();
        final HttpServletRequest request = requestHolder.getHttpServletRequest();
        
        if (request == null)
        {
            throw new IllegalArgumentException(
                    "The HttpServletRequest was not fired as event and thus we can't capture the current contexts!");
        }
        
        // We need a session to be created
        request.getSession(true);

        // TODO: no idea yet if that makes the request scope thread-safe
        BoundBeanStore currentRequestBeanStore = WeldUtils.getBeanStore(httpRequestContext);
        final BoundBeanStore conversationBeanStore = WeldUtils.getBeanStore(httpConversationContext);
        final BoundBeanStore requestBeanStore = (BoundBeanStore) Proxy.newProxyInstance(
                currentRequestBeanStore.getClass().getClassLoader(), new Class<?>[] { BoundBeanStore.class },
                new DelegatingSynchronizingBoundBeanStore(currentRequestBeanStore));
        
        ContextReference contextReference = new ContextReference()
        {
            
            // Setup
            // - SessionHolder.initialize()
            // - Associate
            //  * Request
            //  * Session
            //  * Conversation
            // - Activate
            //  * Request
            //  * Session
            //  * Conversation
            @Override
            public void attach()
            {
                // Use reflection to be compatible with Weld 1
                if (requestInitializedMethod != null)
                {
                    try
                    {
                        requestInitializedMethod.invoke(null, request);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Could not initialize request for thread!", e);
                    }
                }
                
                // NOTE: we need a thread-safe beanStore 
                WeldUtils.setBeanStore(httpRequestContext, requestBeanStore);
                WeldUtils.setBeanStore(httpConversationContext, conversationBeanStore);
                
                httpSessionContext.associate(request);
                httpConversationContext.associate(request);
                httpRequestContext.activate();
                httpSessionContext.activate();
                httpConversationContext.activate();
                // Activating unsets the initialized flag, so we need to set that manually
                WeldUtils.setInitialized(httpConversationContext);
            }

            // Teardown
            // - Deactivate
            //  * Conversation
            //  * Request
            //  * Session
            // - Dissociate
            //  * Request
            //  * Session
            //  * Conversation
            // - SessionHolder.clear()
            @Override
            public void detach()
            {
                // We can't just deactivate since that would destroy transient conversations
                
                // NOTE: unlocking will be done by the lock owning thread
                // httpConversationContext.getCurrentConversation().unlock();
                WeldUtils.setBeanStore(httpConversationContext, null);
                WeldUtils.removeState(httpConversationContext);
                // httpConversationContext.deactivate();
                
                httpRequestContext.deactivate();
                httpSessionContext.deactivate();
                httpRequestContext.dissociate(request);
                httpSessionContext.dissociate(request);
                httpConversationContext.dissociate(request);
                
                // Use reflection to be compatible with Weld 1
                if (clearMethod != null)
                {
                    try
                    {
                        clearMethod.invoke(null);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Could not clear request for thread!", e);
                    }
                }
            }
        };
        
        l.add(contextReference);
        return l;
    }
    
}

