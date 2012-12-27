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

package com.ebupt.webjoin.insight.intercept.topology;

import java.util.Collection;

import com.ebupt.webjoin.insight.color.ColorManager;
import com.ebupt.webjoin.insight.intercept.metrics.AbstractMetricsGenerator;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;

 

/**
 * Provides a default implementation for {@link ExternalResourceAnalyzer}. It
 * is derived from {@link AbstractExternalFramesLocator} in order to ensure that
 * they are both using the same external {@link Frame}s
 */
public abstract class AbstractExternalResourceAnalyzer implements ExternalResourceAnalyzer {
	protected final ColorManager  colorManager;
	private final OperationType opType;

	protected AbstractExternalResourceAnalyzer(OperationType type) {
		if ((opType=type) == null) {
			throw new IllegalStateException("No operation type specified");
		}
		
		colorManager = ColorManager.getInstance();
	}

	public final OperationType getOperationType () {
	    return opType;
	}

	public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace) {
		return locateExternalResourceName(trace, locateFrames(trace));
	}

	public Collection<Frame> locateFrames(Trace trace) {
		return AbstractMetricsGenerator.locateDefaultMetricsFrames(trace, getOperationType());
	}
}
