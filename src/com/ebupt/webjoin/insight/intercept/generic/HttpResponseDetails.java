package com.ebupt.webjoin.insight.intercept.generic;

import org.apache.coyote.Response;

import com.ebupt.webjoin.insight.intercept.operation.OperationList;

public abstract interface HttpResponseDetails {
	public abstract void setResponseInstance(
			Response paramHttpServletResponse);

	public abstract void clearResponseInstance();
	public abstract String getResponseBody();

	public abstract int getStatusCode();

	public abstract String getStatusMessage();

	public abstract long getContentLength();

	public abstract OperationList fillInResponseHeaders(
			OperationList paramOperationList);
	 
}