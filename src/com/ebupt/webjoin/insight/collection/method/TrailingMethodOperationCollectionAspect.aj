package com.ebupt.webjoin.insight.collection.method;

import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.collection.OperationCollector;
import com.ebupt.webjoin.insight.collection.TrailingAbstractOperationCollectionAspect;
import com.ebupt.webjoin.insight.intercept.operation.Operation;

/**
 * Performs a similar functionality like MethodOperationCollectionAspect, but
 * allows for the possibility that the generated {@link Operation} may be
 * eventually discarded 
 */
public abstract aspect TrailingMethodOperationCollectionAspect
        extends TrailingAbstractOperationCollectionAspect {
    protected TrailingMethodOperationCollectionAspect () {
        super();
    }

    public TrailingMethodOperationCollectionAspect(OperationCollector operationCollector) {
        super(operationCollector);
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation op = new Operation();
        JoinPointFinalizer.register(op,  jp);
        return op;
    }

    @Override
    public String getPluginName() {
        return "ebj3";
    }
}
