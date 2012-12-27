package com.ebupt.webjoin.insight.intercept.generic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationMap;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.util.StringFormatterUtils;

public abstract aspect AbstractHttpRequestOperationCollectionAspect extends
		AbstractHttpRequestOperationSupport {
	public abstract pointcut collectionPoint(Request req,
			Response resp);

	public pointcut checkCollectionPoint(Request req,
			Response resp):collectionPoint(req,resp);

	private InterceptConfiguration interceptConfig = InterceptConfiguration
			.getInstance();
	FrameBuilder _builder = interceptConfig.getFrameBuilder();

	Operation fillInOperation(Operation operation, JoinPoint jp,
			Request request, Response response) {
		// true now
		boolean collectExtra = collectExtraInformation();
		OperationMap opRequest = fillInRequestDetails(
				operation.createMap(FrameBuilder.HINT_HTTP_REQUEST), request,
				collectExtra);
		OperationMap opResponse = fillInResponseDetails(
				operation.createMap(FrameBuilder.HINT_HTTP_RESPONSE), response,
				collectExtra);
		fillJoinPointDetails(jp, operation, opRequest, opResponse, request,
				response);

		return operation;
	}

	void packageReqRespInfo(JoinPoint jp, Request req,
			Response resp) {
		Operation operation = new Operation().type(OperationType.HTTP);

		fillInOperation(operation, jp, req, resp);
		// this._builder.exit();
		String requestKey = FrameBuilder.HINT_HTTP_REQUEST;
		String responseKey = FrameBuilder.HINT_HTTP_RESPONSE;
		_builder.setHint(requestKey, operation.get(requestKey));
		_builder.setHint(responseKey, operation.get(responseKey));
	}

	@SuppressAjWarnings({ "adviceDidNotMatch" })
	Object around(Request req, Response resp)
			throws ServletException, IOException :collectionPoint(req,resp)
	{
		Object ret = Void.TYPE;
		ret = proceed(req, resp);
		packageReqRespInfo(thisJoinPoint, req, resp);

		return ret;

	}

	@SuppressWarnings("unchecked")
	public void setHint(String key, byte[] bytes) {
		List<Byte> valueList ;
		if(null == this._builder.getHint(key))
			valueList = new ArrayList<Byte>();
		else
			valueList = (List<Byte>) this._builder.getHint(key);
		for(byte _byte:bytes)
			valueList.add(_byte);
		this._builder.setHint(key, valueList);
	}
	public void setHint(String key, boolean isStatic) {
		this._builder.setHint(key,isStatic);
	}
	@SuppressAjWarnings({ "adviceDidNotMatch" })
	after(Request req, Response resp)throwing(Throwable exp):checkCollectionPoint(req,resp)
	{
		packageReqRespInfo(thisJoinPoint, req, resp);
		_builder.setHint(FrameBuilder.HINT_HAS_EXCEPTION, 1);
		_builder.setHint("exception",
				StringFormatterUtils.formatStackTrace(exp));

	}

}
