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
package org.apache.deltaspike.cdise.api;

/**
 * <p>A ContextReference references the context of CDI scopes of a thread.
 * It allows attaching and detaching the context to a different thread.</p>
 *
 * <p>The intention is to provide a portable way of reusing a context in
 * a new thread without having to manually setup beans.</p>
 */
public interface ContextReference
{

    /**
     * <p>Attaches the context to the current thread.</p>
     */
    void attach();

    /**
     * <p>Detaches the context from the current thread.</p>
     */
    void detach();
}
