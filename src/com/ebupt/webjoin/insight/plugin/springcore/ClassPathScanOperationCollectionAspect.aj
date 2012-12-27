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


public aspect ClassPathScanOperationCollectionAspect extends SpringLifecycleMethodOperationCollectionAspect {
    public ClassPathScanOperationCollectionAspect() {
        super();
    }

    public pointcut findPathMatchingResources()
        : execution(* org.springframework.core.io.support.PathMatchingResourcePatternResolver+.findPathMatchingResources(..));

    public pointcut scan()
        : execution(* org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider+.findCandidateComponents(..));

    public pointcut contextRefresh()
        : execution(* org.springframework.context.ConfigurableApplicationContext+.refresh(..));

    public pointcut collectionPoint() :
        findPathMatchingResources() ||
        scan() ||
        contextRefresh();
}

