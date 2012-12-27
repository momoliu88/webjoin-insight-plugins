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

import com.ebupt.webjoin.insight.collection.method.MethodOperationsCollected;
import org.springframework.stereotype.Repository;

/**
 * Causes all {@link org.springframework.stereotype.Repository} annotated classes to be instrumented
 */
public aspect RepositoryMethodOperationCollectionAspect {
	/*
	 * We exclude all Insight beans since if we want insight-on-insight we
	 * cannot use this aspect as it may cause infinite recursion
	 */
    declare @type: (@Repository *) && !(com.ebupt.webjoin.insight..*) : @MethodOperationsCollected;
}
