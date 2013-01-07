package com.ebupt.webjoin.insight.collection.method;

public aspect AnnotationDrivenMethodOperationCollectionAspect extends
		MethodOperationCollectionAspect {
	public AnnotationDrivenMethodOperationCollectionAspect() {
		super();
	}
//	pointcut tracemethod():execution(* (@MethodOperationsCollected *).*(..))||call(* (@MethodOperationsCollected *).*(..));
	public pointcut collectionPoint() : execution(* (@MethodOperationsCollected *).*(..));

//	before():tracemethod(){
//		System.out.println("methodoperationcollected "+thisJoinPoint);
//	}
	@Override
	public boolean isEndpoint() {
		return true;
	}

	@Override
	public String getPluginName() {
		return null;
	}
}
