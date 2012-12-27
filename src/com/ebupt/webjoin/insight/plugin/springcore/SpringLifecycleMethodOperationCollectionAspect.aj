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

import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.collection.method.MethodOperationCollectionAspect;
import com.ebupt.webjoin.insight.intercept.endpoint.EndPointAnalysis;
import com.ebupt.webjoin.insight.intercept.operation.Operation;

/**
 * 
 */
public abstract aspect SpringLifecycleMethodOperationCollectionAspect extends
		MethodOperationCollectionAspect {
	/**
	 * The <U>static</U> score assigned to operations that do not contain
	 * interesting enough Spring core API(s) - it is just slightly above that of
	 * a servlet and/or queue operation
	 */
	public static final int LIFECYCLE_SCORE = EndPointAnalysis.CEILING_LAYER_SCORE + 1;

	protected SpringLifecycleMethodOperationCollectionAspect() {
		super();
	}

	@Override
	protected Operation createOperation(JoinPoint jp) {
		return super.createOperation(jp).put(EndPointAnalysis.SCORE_FIELD,
				LIFECYCLE_SCORE);
	}

	@Override
	public String getPluginName() {
		return SpringCorePluginRuntimeDescriptor.PLUGIN_NAME;
	}
}
