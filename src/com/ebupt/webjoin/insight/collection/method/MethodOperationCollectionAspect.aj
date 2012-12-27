package com.ebupt.webjoin.insight.collection.method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import com.ebupt.webjoin.insight.collection.AbstractOperationCollectionAspect;
import com.ebupt.webjoin.insight.collection.OperationCollector;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
 

public abstract aspect MethodOperationCollectionAspect extends AbstractOperationCollectionAspect {

    protected MethodOperationCollectionAspect() {
        super();
    }

    protected MethodOperationCollectionAspect(OperationCollector collector) {
        super(collector);
    }

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Operation op = new Operation();
        op.sourceCodeLocation(getSourceCodeLocation(jp));
        String className = null;
        String methodName = "";
        if (null != jp.getTarget())
         className = jp.getTarget().getClass().getSimpleName();
        if( null != jp.getSignature())
        	if(jp.getSignature() instanceof MethodSignature)
        	{
        		MethodSignature methodSig = (MethodSignature)jp.getSignature();
        		methodName = methodSig.getMethod().getName();
        	}
        	else
        		methodName = jp.getSignature().getName();
        className = className==null?"":(className+"#");
        op.label(className+methodName);
        JoinPointFinalizer.register(op, jp);
        return op;
    }
}