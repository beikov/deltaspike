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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@ApplicationScoped
public class ContextCaptureProducerBean
{
    
    private final ThreadLocal<String> data1 = new ThreadLocal<String>();
    private final ThreadLocal<String> data2 = new ThreadLocal<String>();
    private final ThreadLocal<String> data3 = new ThreadLocal<String>();
    private final ThreadLocal<String> data4 = new ThreadLocal<String>();
    private final ThreadLocal<String> data5 = new ThreadLocal<String>();
    private final ThreadLocal<String> data6 = new ThreadLocal<String>();
    private final ThreadLocal<String> data7 = new ThreadLocal<String>();
    private final ThreadLocal<String> data8 = new ThreadLocal<String>();

    @Named
    @RequestScoped
    @Produces
    StringHolder getData1()
    {
        return new DefaultStringHolder(data1.get());
    }
    
    @Named
    @ConversationScoped
    @Produces
    StringHolder getData2()
    {
        return new DefaultStringHolder(data2.get());
    }
    
    @Named
    @SessionScoped
    @Produces
    StringHolder getData3()
    {
        return new DefaultStringHolder(data3.get());
    }
    
    @Named
    @ApplicationScoped
    @Produces
    StringHolder getData4()
    {
        return new DefaultStringHolder(data4.get());
    }
    @Named
    @RequestScoped
    @Produces
    StringHolder getData5()
    {
        return new DefaultStringHolder(data5.get());
    }
    
    @Named
    @ConversationScoped
    @Produces
    StringHolder getData6()
    {
        return new DefaultStringHolder(data6.get());
    }
    
    @Named
    @SessionScoped
    @Produces
    StringHolder getData7()
    {
        return new DefaultStringHolder(data7.get());
    }
    
    @Named
    @ApplicationScoped
    @Produces
    StringHolder getData8()
    {
        return new DefaultStringHolder(data8.get());
    }
    
    /*
     * Getter and setter
     */
    
    public void setData1(String data)
    {
        data1.set(data);
    }
    
    public void clearData1()
    {
        data1.remove();
    }
    
    public void setData2(String data)
    {
        data2.set(data);
    }
    
    public void clearData2()
    {
        data2.remove();
    }
    
    public void setData3(String data)
    {
        data3.set(data);
    }
    
    public void clearData3()
    {
        data3.remove();
    }
    
    public void setData4(String data)
    {
        data4.set(data);
    }
    
    public void clearData4()
    {
        data4.remove();
    }
    
    public void setData5(String data)
    {
        data5.set(data);
    }
    
    public void clearData5()
    {
        data5.remove();
    }
    
    public void setData6(String data)
    {
        data6.set(data);
    }
    
    public void clearData6()
    {
        data6.remove();
    }
    
    public void setData7(String data)
    {
        data7.set(data);
    }
    
    public void clearData7()
    {
        data7.remove();
    }
    
    public void setData8(String data)
    {
        data8.set(data);
    }
    
    public void clearData8()
    {
        data8.remove();
    }
    
    /*
     * Thread local getter
     */
    
    public ThreadLocal<String> getData5Holder()
    {
        return data5;
    }
    
    public ThreadLocal<String> getData6Holder()
    {
        return data6;
    }
    
    public ThreadLocal<String> getData7Holder()
    {
        return data7;
    }
    
    public ThreadLocal<String> getData8Holder()
    {
        return data8;
    }
}
