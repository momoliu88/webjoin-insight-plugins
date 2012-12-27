package com.ebupt.webjoin.insight.intercept.trace;

import java.util.List;

public  interface TraceErrorAnalyzer {
	public  List<TraceError> locateErrors(Trace paramTrace);
}