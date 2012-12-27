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

package com.ebupt.webjoin.insight.collection;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.MethodSignature;

import com.ebupt.webjoin.insight.intercept.operation.Operation;

/**
 * An abstract aspect which consolidates boiler plate code.
 * 
 * This aspect is woven into classes (based on a subclass overriding the
 * collectionPoint) and interacts with the {@link OperationCollector} on behalf
 * of the subclass.
 * 
 * Not all types of aspects will work as a subclass of this class. Aspects which
 * must interact with the annotations of a JoinPoint will not be able to make
 * use of this abstract aspect (as the pointcut does not capture the
 * annotations)
 */
public abstract aspect AbstractOperationCollectionAspect extends
		OperationCollectionAspectSupport {
	protected AbstractOperationCollectionAspect() {
		super();
	}

	protected AbstractOperationCollectionAspect(OperationCollector collector) {
		super(collector);
	}

	/**
	 * Overridden by subclass to specify a pointcut that we will interact with.
	 */
	public abstract pointcut collectionPoint();

	public pointcut collectionStrategyPoint()
        : collectionPoint() &&!within(com.ebupt.webjoin.insight..*)
        && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart));

	/**
	 * Enter a frame before a collection point is called.
	 */
	@SuppressAjWarnings("adviceDidNotMatch")
	before() : collectionStrategyPoint() {
		
		getCollector().enter(createOperation(thisJoinPoint));
/*	DefaultOperationCollector collector = (DefaultOperationCollector)getCollector();	
 * System.out.println("before [" + thisJoinPoint + "]enter,size:" + collector.builder.getdepth());
*/	}

	/**
	 * Inform the metric collector that a method has returned
	 */
	@SuppressAjWarnings("adviceDidNotMatch")
	after() returning(Object returnValue) : collectionStrategyPoint() {
		// We want to capture difference between not having a return value and
		// having a null return value
	//	System.out.println("after [" + thisJoinPoint + "]exit");
		if (thisJoinPointStaticPart.getSignature() instanceof MethodSignature)
			if (((MethodSignature) thisJoinPointStaticPart.getSignature())
					.getReturnType() == void.class) {
				getCollector().exitNormal();
			} else {
				getCollector().exitNormal(returnValue);
			}
/*		DefaultOperationCollector collector = (DefaultOperationCollector)getCollector();
		System.out.println("after returning [" + thisJoinPoint + "]exit"+" stack size: "+collector.builder.getdepth());
*/	}

	/**
	 * Inform the metric collector that a method has thrown an exception
	 */
	@SuppressAjWarnings("adviceDidNotMatch")
	after() throwing(Throwable exception) : collectionStrategyPoint() {
/*		System.out.println("after throwing");
*/		getCollector().exitAbnormal(exception);
/*		DefaultOperationCollector collector = (DefaultOperationCollector)getCollector();
		System.out.println("after throwing[" + thisJoinPoint + "]exit"+" stack size: "+collector.builder.getdepth());
*/	}

	/**
	 * Overriden by subclass to create an Operation which represents the given
	 * JoinPoint
	 */
	protected abstract Operation createOperation(JoinPoint jp);

}
