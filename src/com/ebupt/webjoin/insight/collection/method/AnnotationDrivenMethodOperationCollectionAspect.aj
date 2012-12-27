package com.ebupt.webjoin.insight.collection.method;

public aspect AnnotationDrivenMethodOperationCollectionAspect extends
		MethodOperationCollectionAspect {
	public AnnotationDrivenMethodOperationCollectionAspect() {
		super();
	}

	public pointcut collectionPoint() : execution(* (@MethodOperationsCollected *).*(..));

	@Override
	public boolean isEndpoint() {
		return true;
	}

	@Override
	public String getPluginName() {
		return null;
	}
}
