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

package com.ebupt.webjoin.insight.plugin.springcore;

 
import com.ebupt.webjoin.insight.collection.method.AnnotationDrivenMethodOperationCollectionAspect;

 
/**
 * Adds the @MethodOperationCollected annotation to most implementations of
 * ApplicationListener.onApplicationEvent().   
 * 
 * We specifically exclude {@link SourceFilteringListener} and {@link GenericApplicationListenerAdapter},
 * as these provide no real value in a frame stack.  In conjunction with the TopLevelMethodEndPointAnalyzer,
 * removing these 2 listeners allows a user's delegate listener to be created as an EndPoint.
 * 
 * @see AnnotationDrivenMethodOperationCollectionAspect
 * 
 * @gitBadge  LongestClassName  On October 04, 2010:  you won the LongestClassName badge
 *            with 54 characters!
 */
public aspect ApplicationListenerMethodOperationCollectionAspect extends SpringLifecycleMethodOperationCollectionAspect {
    public ApplicationListenerMethodOperationCollectionAspect() {
        super();
    }

    public pointcut appListener()
        : execution(* org.springframework.context.ApplicationListener+.onApplicationEvent(..));

    public pointcut delegatingGenericListenerAdapter()
        : execution(* org.springframework.context.event.GenericApplicationListenerAdapter+.onApplicationEvent(..));

    public pointcut sourceFilteringListener()
        : execution(* org.springframework.context.event.SourceFilteringListener+.onApplicationEvent(..));

    public pointcut collectionPoint() : 
        appListener() && 
        !delegatingGenericListenerAdapter() && 
        !sourceFilteringListener() 
       && !AnnotationDrivenMethodOperationCollectionAspect.collectionPoint();
}

