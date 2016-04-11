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
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.deltaspike.cdise.api.ContextReference;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ConversationContext;
import org.apache.webbeans.spi.ContextsService;

@Dependent
public class OwbContextCapturer
{
    
    private static final Method WEB_CONTEXTS_REMOVE_THREAD_LOCALS;
    private static final Class<?> CDI_APP_CONTEXTS_CLASS;
    private static final Method CDI_APP_CONTEXTS_REMOVE_THREAD_LOCALS;
    private static final Method REQUEST_INTERCEPTOR_REMOVE_THREAD_LOCALS;
    
    static
    {
        Method webContextsMethod = null;
        Class<?> cdiAppContextsClazz = null;
        Method cdiAppContextsMethod = null;
        
        try
        {
            Class<?> webContexts = Class.forName("org.apache.webbeans.web.context.WebContextsService");
            
            try
            {
                webContextsMethod = webContexts.getMethod("removeThreadLocals");
            }
            catch (Exception e)
            {
                throw new RuntimeException("Could not initialize context capture for OpenWebBeans!", e);
            }
        }
        catch (ClassNotFoundException e)
        {
            // Apparently we don't have WebContextsService in this environment
        }
        try
        {
            cdiAppContextsClazz = Class.forName("org.apache.openejb.cdi.CdiAppContextsService");
            
            try
            {
                cdiAppContextsMethod = cdiAppContextsClazz.getMethod("removeThreadLocals");
            }
            catch (Exception e)
            {
                throw new RuntimeException("Could not initialize context capture for OpenWebBeans!", e);
            }
        }
        catch (ClassNotFoundException e)
        {
            // Apparently we don't have CdiAppContextsService in this environment
        }
        
        try
        {
            Class<?> clazz = Class.forName("org.apache.webbeans.web.intercept.RequestScopedBeanInterceptorHandler");
            REQUEST_INTERCEPTOR_REMOVE_THREAD_LOCALS = clazz.getMethod("removeThreadLocals");
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not initialize context capture for OpenWebBeans!", e);
        }
        
        if (webContextsMethod == null && cdiAppContextsMethod == null)
        {
            throw new RuntimeException("Unsupported environment for context capture for OpenWebBeans!");
        }
        
        WEB_CONTEXTS_REMOVE_THREAD_LOCALS = webContextsMethod;
        CDI_APP_CONTEXTS_CLASS = cdiAppContextsClazz;
        CDI_APP_CONTEXTS_REMOVE_THREAD_LOCALS = cdiAppContextsMethod;
    }

    @Inject
    private ServletContextHolder servletContextHolder;
    @Inject
    private HttpServletRequestHolder requestHolder;
    
    private ContextsService getContextsService()
    {
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        return webBeansContext.getContextsService();
    }

    public List<ContextReference> captureContexts()
    {
        List<ContextReference> l = new ArrayList<ContextReference>();
        final ContextsService contextsService = getContextsService();
        final HttpServletRequest request = requestHolder.getHttpServletRequest();
        final ServletContext servletContext = servletContextHolder.getServletContext();
        
        if (request == null)
        {
            throw new IllegalArgumentException(
                    "The HttpServletRequest was not fired as event and thus we can't capture the current contexts!");
        }
        
        if (servletContext == null)
        {
            throw new IllegalArgumentException(
                    "The ServletContext was not fired as event and thus we can't capture the current contexts!");
        }
        
        final ServletRequestEvent requestEvent = new ServletRequestEvent(servletContext, request);
        // We need a session to be created
        final HttpSession session = request.getSession(true);
        final ConversationContext conversationContext = (ConversationContext) contextsService
                .getCurrentContext(ConversationScoped.class);
        
        final Object requestComponentInstanceMap = OwbUtils
                .getComponentInstanceMap(contextsService.getCurrentContext(RequestScoped.class));
        final Object conversationComponentInstanceMap = OwbUtils
                .getComponentInstanceMap(contextsService.getCurrentContext(ConversationScoped.class));
        final Object sessionComponentInstanceMap = OwbUtils
                .getComponentInstanceMap(contextsService.getCurrentContext(SessionScoped.class));

        ContextReference contextReference = new ContextReference()
        {

            @Override
            public void attach()
            {
                contextsService.startContext(ApplicationScoped.class, null);
                contextsService.startContext(Singleton.class, null);
                contextsService.startContext(RequestScoped.class, requestEvent);
                contextsService.startContext(SessionScoped.class, session);
                contextsService.startContext(ConversationScoped.class, conversationContext);

                OwbUtils.setComponentInstanceMap(contextsService.getCurrentContext(RequestScoped.class),
                        requestComponentInstanceMap);
                OwbUtils.setComponentInstanceMap(contextsService.getCurrentContext(ConversationScoped.class),
                        conversationComponentInstanceMap);
                OwbUtils.setComponentInstanceMap(contextsService.getCurrentContext(SessionScoped.class),
                        sessionComponentInstanceMap);
            }

            @Override
            public void detach()
            {
                // Removes thread-locals
                if (CDI_APP_CONTEXTS_REMOVE_THREAD_LOCALS != null && CDI_APP_CONTEXTS_CLASS.isInstance(contextsService))
                {
                    try
                    {
                        CDI_APP_CONTEXTS_REMOVE_THREAD_LOCALS.invoke(contextsService);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Could not reset thread locals for OpenWebBeans!", e);
                    }
                }

                if (WEB_CONTEXTS_REMOVE_THREAD_LOCALS != null)
                {
                    try
                    {
                        WEB_CONTEXTS_REMOVE_THREAD_LOCALS.invoke(null);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Could not reset thread locals for OpenWebBeans!", e);
                    }
                }
                if (REQUEST_INTERCEPTOR_REMOVE_THREAD_LOCALS != null)
                {
                    try
                    {
                        REQUEST_INTERCEPTOR_REMOVE_THREAD_LOCALS.invoke(null);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException("Could not reset thread locals for OpenWebBeans!", e);
                    }
                }
            }
        };
        
        
        l.add(contextReference);
        return l;
    }
    
}
