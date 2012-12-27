package com.ebupt.webjoin.insight.intercept.generic;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
/*import java.util.logging.Level;
import java.util.logging.Logger;*/
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
/*import javax.servlet.ServletContext;
*/import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/*import javax.servlet.http.HttpSession;
*/
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.tomcat.util.http.Cookies;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.http.Parameters;
import org.apache.tomcat.util.http.ServerCookie;
import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationList;
import com.ebupt.webjoin.insight.intercept.operation.OperationMap;
import com.ebupt.webjoin.insight.intercept.operation.OperationUtils;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.intercept.trace.TraceId;
import com.ebupt.webjoin.insight.util.StringUtil;

public abstract class AbstractHttpRequestOperationSupport {
	public static final String TRACE_ID_HEADER_NAME = "X-TraceId";
	public static final String TRACE_URL_HEADER_NAME = "X-TraceUrl";
	public static final String TRACE_URL_PATH_PREFIX = "/insight/services/traces/";
	public static final String FORCE_INSIGHT_TRACEID_HDR_PROPNAME = "insight.forceTraceIdHeader";
	public static InterceptConfiguration interceptConfig = InterceptConfiguration
			.getInstance();
	public static FrameBuilder frameBuilder = interceptConfig.getFrameBuilder();

	public static final Map<String, String> STATUS_REASONS = Collections
			.unmodifiableMap(new TreeMap<String, String>());

	protected abstract HttpResponseDetails createHttpResponseDetailsInstance();

	protected abstract Servlet retrieveInvokedServlet(JoinPoint paramJoinPoint,
			Request paramHttpServletRequest,
			Response paramHttpServletResponse);

	private final ThreadLocal<HttpResponseDetails> _rspHandler = new ThreadLocal<HttpResponseDetails>() {
		protected HttpResponseDetails initialValue() {
			return AbstractHttpRequestOperationSupport.this
					.createHttpResponseDetailsInstance();
		}
	};

	protected final InterceptConfiguration configuration = InterceptConfiguration
			.getInstance();

	protected AbstractHttpRequestOperationSupport() {
		 
	}

	protected boolean collectExtraInformation() {
		return true;
	}

	void addTraceIdResponseHeader(HttpServletResponse response, TraceId traceId) {
		response.addHeader("X-TraceId", traceId.toString());
	}

	OperationMap fillInRequestDetails(OperationMap op,
			Request request, boolean collectExtra) {
		fillInRequestNetworkDetails(op, request, collectExtra);
		if (collectExtra) {
			fillInRequestHeaders(op.createList("headers"), request);
			fillInRequestCookies(op.createList("cookies"), request);

			fillInQueryParams(op.createList("queryParams"), request);
			fillInAttributes(op.createList("attributes"), request);
		}
		return op;
	}

	OperationList fillInAttributes(OperationList attributes, Request request) {
		Map<String,Object> attrs= request.getAttributes();
		for(Entry<String,Object> entry:attrs.entrySet())
		{
			OperationUtils.addNameValuePair(attributes, entry.getKey(), entry.getValue().toString());
		}
		return attributes;
	}

	OperationMap fillInRequestNetworkDetails(OperationMap op,
			Request request, boolean collectExtra) {
	//	Principal userPrincipal = request.getUserPrincipal();
		op.put("method", request.method().getString())
				.put("uri", request.requestURI().getString());
		if (collectExtra) {
			op.put("protocol", request.protocol().getString())
					.put("queryString", request.queryString().getString())
					//.put("locale", request.getLocale().toString())
					.put("remoteAddr", request.remoteAddr().toString())
					.put("remotePort", request.getRemotePort())
					.put("localAddr", request.localAddr().getString())
					.put("localPort", request.getServerPort());	 	 
		}

		int contentLength = request.getContentLength();
		contentLength = contentLength < 0 ? 0 : contentLength;
		op.put("contentLength", contentLength);

		return op;
	}
	OperationList fillInRequestCookies(OperationList cookies,Request request)
	{
		Cookies requestCookies = request.getCookies();
		int count = requestCookies.getCookieCount();
		for(int i = 0 ;i < count; i++)
		{
			ServerCookie cookie = requestCookies.getCookie(i);
			OperationUtils.addNameValuePair(cookies, cookie.getName().toString(),
					cookie.getValue().toString());
		}
		return cookies;
	}
	OperationList fillInRequestHeaders(OperationList headers,
			Request request) {
		MimeHeaders requestHeaders = request.getMimeHeaders();
		Enumeration<String> names = requestHeaders.names();
		while(names != null && names.hasMoreElements())
		{
			String name = names.nextElement();
			String value = requestHeaders.getHeader(name);
			if(value != null)
				OperationUtils.addNameValuePair(headers, name,
						value);
		}
		/*Enumeration<String> values;
		for (Enumeration<String> names = request.getHeaderNames(); (names != null)
				&& (names.hasMoreElements());) {
			String name = (String) names.nextElement();
			values = request.getHeaders(name);
			while (values != null && values.hasMoreElements())
				OperationUtils.addNameValuePair(headers, name,
						values.nextElement());
		}*/

		return headers;
	}

	OperationList fillInQueryParams(OperationList params,
			Request request) {
	    Parameters parameters = request.getParameters();
		/*int j;
		for (Iterator<Entry<String, String[]>> localIterator = m.entrySet()
				.iterator(); localIterator.hasNext();) {
			Entry<String, String[]> e = localIterator.next();
			String paramName = e.getKey();
			String[] arrayOfString;
			j = (arrayOfString = e.getValue()).length;
			int i = 0;
			while (i < j) {
				String paramValue = arrayOfString[i];
				OperationUtils.addNameValuePair(params, paramName, paramValue);
				i++;
			}

		}*/
		Enumeration<String> names = parameters.getParameterNames();
		while(names != null && names.hasMoreElements())
		{
			String name = names.nextElement();
			String [] values = parameters.getParameterValues(name);
			for(String value:values)
				OperationUtils.addNameValuePair(params, name, value);
		}
		return params;
	}

	OperationMap fillInResponseDetails(OperationMap op,
			Response response, boolean collectExtra) {
		HttpResponseDetails details = this._rspHandler.get();
		try {
			details.setResponseInstance(response);

			int statusCode = details.getStatusCode();
			op.put("statusCode", statusCode).put("contentLength",
					details.getContentLength());
 			if (collectExtra) {
				op.put("reasonPhrase",
						resolveReasonPhrase(statusCode,
								details.getStatusMessage()));
				details.fillInResponseHeaders(op.createList("headers"));
			}
		}  finally {
			details.clearResponseInstance();
		}

		return op;
	}

	protected void fillJoinPointDetails(JoinPoint jp, Operation rootOperation,
			OperationMap opRequest, OperationMap opResponse,
			Request request,Response response) {
		fillServletDetails(retrieveInvokedServlet(jp, request, response),
				rootOperation, opRequest, opResponse, request, response);
	}

	protected void fillServletDetails(Servlet servlet, Operation rootOperation,
			OperationMap opRequest, OperationMap opResponse,
			 Request request, Response response) {
		opRequest.put("servletName", resolveServletName(servlet, request));
	}

	String resolveServletName(Servlet servlet, Request request) {
		ServletConfig config = servlet == null ? null : servlet
				.getServletConfig();
		if (config == null) {
		/*	this._logger.warning("resolveServletName("
					+ request.getRequestURI() + ") no configuration available");*/
			return parseServletName(request);
		}

		return config.getServletName();
	}

	String parseServletName(Request request) {
		String servletPath = normalizeServletPath(request.serverName().getString());

		if (!StringUtil.hasText(servletPath)) {
			/*HttpSession session = request.getSession(false);
			ServletContext context = session == null ? null : session
					.getServletContext();
			servletPath = context == null ? ""
					: normalizeServletPath(normalizeContextPath(context
							.getContextPath()));*/
			servletPath="";
		}

		if (StringUtil.hasText(servletPath)) {
			return servletPath;
		}

		return "Unknown";
	}

	String resolveReasonPhrase(int statusCode, String statusMsg) {
		if (StringUtil.hasText(statusMsg)) {
			return statusMsg;
		}

		String strCode = String.valueOf(statusCode);
		String msg = (String) STATUS_REASONS.get(strCode);
		if (msg != null) {
			return msg;
		}
		return strCode;
	}

	String makeOperationLabel(HttpServletRequest request) {
		String queryString = request.getQueryString();
		if(queryString == null)
			return request.getMethod() + " " + request.getRequestURI();
		queryString = "?" + queryString;
		return request.getMethod() + " " + request.getRequestURI()
				+ queryString;
	}

	String normalizeContextPath(String contextPath) {
		return contextPath.length() > 0 ? contextPath.substring(1) : "ROOT";
	}

	String normalizeServletPath(String path) {
		if (!StringUtil.hasText(path)) {
			return path;
		}

		String servletPath = path;

		if (servletPath.charAt(0) == '/') {
			servletPath = servletPath.substring(1);
		}

		int idx = servletPath.indexOf('/');
		if (idx > 0) {
			return servletPath.substring(0, idx);
		}
		return servletPath;
	}

	boolean canAddTraceIdResponseHeader() {
		return (interceptConfig.isDevEdition());// || (this._forceTraceIdHdr);
	}
}