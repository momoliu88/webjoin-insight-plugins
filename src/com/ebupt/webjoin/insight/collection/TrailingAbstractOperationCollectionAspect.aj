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

import java.util.Stack;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.MethodSignature;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.util.KeyValPair;



/**
 * This aspect differs from the {@link AbstractOperationCollectionAspect} by
 * the fact that while the {@link Operation} is generated in the <code>before</code>
 * advice, it is added to the collector only if non-<code>null. Otherwise a special
 * &quot;placeholder&quot; is used to signal the <code>after</code> advice <U>not</U>
 * to call {@link OperationCollector} <code>exit</code> methods (since the
 * <code>before</code> advice does not call the {@link OperationCollector#enter(Operation)}
 * if <code>null</code> {@link Operation} is returned by {@link #createOperation(JoinPoint)})
 */
public abstract aspect TrailingAbstractOperationCollectionAspect
        extends OperationCollectionAspectSupport {
    protected static final Operation    NULL_PLACEHOLDER=new Operation().type(OperationType.valueOf("null"));

    protected TrailingAbstractOperationCollectionAspect () {
        super();
    }

    protected TrailingAbstractOperationCollectionAspect(OperationCollector operationCollector) {
        super(operationCollector);
    }


    public pointcut collectionStrategyPoint()
        : collectionPoint() && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart));

    @SuppressAjWarnings({"adviceDidNotMatch"})
    before () : collectionStrategyPoint() {
        final Operation             op=createOperation(thisJoinPoint);
        final OperationCollector    collector=getEnterCollector(op);
        enterOperation(collector, op);
    }

    protected Operation enterOperation (OperationCollector collector, Operation op) {
        if (collector != null) {
            collector.enter(op);
            push(op);
            return op;
        } else {
            push(NULL_PLACEHOLDER);
            return NULL_PLACEHOLDER;
        }
    }

    // returns null if {@link OperationCollector#enter(Operation)} should not be called
    protected OperationCollector getEnterCollector (Operation op) {
        if (op == null) {
            return null;
        }

        return getCollector();
    }

    /**
     * Inform the metric collector that a method has returned
     */
    @SuppressAjWarnings({"adviceDidNotMatch"})
    after() returning(Object returnValue) : collectionStrategyPoint() {
    	KeyValPair<OperationCollector,Operation>	kvp=getExitCollector(thisJoinPoint, returnValue, null);
    	OperationCollector							collector=(kvp == null) ? null : kvp.getKey();
    	Operation									op=(kvp == null) ? null : kvp.getValue();
        if (getReturnType(thisJoinPointStaticPart.getSignature()) == void.class) {
        	exitOperation(collector, op, null, false, null);
        } else {
        	exitOperation(collector, op, returnValue, true, null);
        }
    }
    /**
     * Inform the metric collector that a method has thrown an exception
     */
    @SuppressAjWarnings({"adviceDidNotMatch"})
    after() throwing(Throwable exception) : collectionStrategyPoint() {
    	KeyValPair<OperationCollector,Operation>	kvp=getExitCollector(thisJoinPoint, null, exception);
    	OperationCollector							collector=(kvp == null) ? null : kvp.getKey();
    	Operation									op=(kvp == null) ? null : kvp.getValue();
    	exitOperation(collector, op, null, false, exception);
    }

    protected void exitOperation (OperationCollector collector, Operation op, Object returnValue, boolean validReturn, Throwable exception) {
        if (collector == null) {    // no operation created
            return;
        }

        if (exception == null) {
        	if (validReturn) {
        		collector.exitNormal(returnValue);
        	} else {
        		collector.exitNormal();
        	}
        } else {
        	collector.exitAbnormal(exception);
        }
    }

    // returns null if the OperationCollector exit method(s) should not be called
    protected KeyValPair<OperationCollector,Operation> getExitCollector(JoinPoint jp, Object returnValue, Throwable exception) {
        return getExitCollector(pop(), jp, returnValue, exception);
    }

    protected KeyValPair<OperationCollector,Operation> getExitCollector(Operation op,JoinPoint jp, Object returnValue, Throwable exception) {
        if (op == NULL_PLACEHOLDER) {
            return null;    // debug breakpoint
        } else {
        	return new KeyValPair<OperationCollector, Operation>(getCollector(), op);
        }
    }
    /**
     * Used to replace a <code>null</code> {@link Operation} returned by
     * the call to {@link #createOperation(JoinPoint)}
     */
    private static final ThreadLocal<Stack<Operation>>  _pendingOperations=new ThreadLocal<Stack<Operation>>() {
            /*
             * @see java.lang.ThreadLocal#initialValue()
             */
            @Override
            protected Stack<Operation> initialValue() {
                return new Stack<Operation>();
            }
        };
    private static final void push (Operation op) {
        final Stack<Operation>  opers=_pendingOperations.get();
        opers.push(op);
    }

    private static final Operation pop () {
        final Stack<Operation>  opers=_pendingOperations.get();
        return opers.pop();
    }
    /**
     * @return The last {@link Operation} created by the <code>before</code>
     * advice - may be <code>null</code> if no pending operation or the
     * {@link #NULL_PLACEHOLDER} if the original {@link #createOperation(JoinPoint)}
     * returned <code>null</code>
     */
    protected static final Operation getPendingOperation () {
        final Stack<Operation>  opers=_pendingOperations.get();
        if (opers.isEmpty()) {
            return null;
        }
        return opers.peek();
    }

    // TODO move it to some 'util' class/JAR
    public static final Class<?> getReturnType (final Signature sig) {
        if (sig == null) {
            return null;
        }

        if (sig instanceof MethodSignature) {
            return ((MethodSignature) sig).getReturnType();
        }

        // usually a constructor...
        return sig.getDeclaringType();
    }
    /**
     * @param The intercepted {@link JoinPoint}
     * @return The {@link Operation} which represents the given
     * {@link JoinPoint} - ignored if <code>null</code>
     */
    protected abstract Operation createOperation(JoinPoint jp);
    /**
     * Overridden by subclass to specify a pointcut that we will interact with.
     */
    public abstract pointcut collectionPoint();

}
