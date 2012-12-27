package com.ebupt.webjoin.insight.intercept.generic;

import java.util.Enumeration;


import org.apache.coyote.Response;
import org.apache.tomcat.util.http.MimeHeaders;

import com.ebupt.webjoin.insight.intercept.operation.OperationList;
import com.ebupt.webjoin.insight.intercept.operation.OperationUtils;

public class HttpResponseDetailsImpl implements HttpResponseDetails {
	private Response response = null;

	public HttpResponseDetailsImpl() {
	};

	public HttpResponseDetailsImpl(Response resp) {
		this.response = resp;
	};

	@Override
	public void setResponseInstance(Response paramHttpServletResponse) {
		this.response = paramHttpServletResponse;
	}

	@Override
	public void clearResponseInstance() {
		this.response = null;
	}

	@Override
	public int getStatusCode() {
		return response.getStatus();
	}

	@Override
	public String getStatusMessage() {
		return null;
	}

	@Override
	public long getContentLength() {
		return response.getContentLength();
	}

	@Override
	public OperationList fillInResponseHeaders(OperationList paramOperationList) {
		MimeHeaders headers = response.getMimeHeaders();
		Enumeration<String> names = headers.names();
		while (names != null && names.hasMoreElements()) {
			String name = names.nextElement();
			String values = headers.getHeader(name);
			OperationUtils.addNameValuePair(paramOperationList, name, values);

		}
		// List<Object> list = paramOperationList.asList();
		/*
		 * System.out.println("===================headers========================="
		 * ); for(Object obj:list) { System.out.println(obj); }
		 * System.out.println(response.getHeader("Content-Type"));
		 */
		return paramOperationList;
	}

	@Override
	public String getResponseBody() {
		// TODO Auto-generated method stub
		return null;
	}

}
