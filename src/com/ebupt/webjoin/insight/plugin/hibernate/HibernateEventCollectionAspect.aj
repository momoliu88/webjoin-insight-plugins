/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ebupt.webjoin.insight.plugin.hibernate;

import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.collection.method.MethodOperationCollectionAspect;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
 

public aspect HibernateEventCollectionAspect extends MethodOperationCollectionAspect {
    public pointcut dirtyCheck()
        : execution(void org.hibernate.event.DirtyCheckEventListener.onDirtyCheck(..));

    public pointcut flushEvent()
        : execution(void org.hibernate.event.FlushEventListener.onFlush(..));

    public pointcut abstractPrepareFlushing()
        : execution(*  org.hibernate.event.def.AbstractFlushingEventListener.prepare*(..));

    public pointcut abstractFlushing()
        : execution(*  org.hibernate.event.def.AbstractFlushingEventListener.flush*(..));

    public pointcut collectionPoint()
        : dirtyCheck() || flushEvent() || abstractPrepareFlushing() || abstractFlushing();

    @Override
    public Operation createOperation(JoinPoint jp) {
        return super.createOperation(jp)
                    .label("Hibernate " + jp.getStaticPart().getSignature().getName())
                    ;
    }

    @Override
    public String getPluginName() {
        return "hibernate";
    }

}
