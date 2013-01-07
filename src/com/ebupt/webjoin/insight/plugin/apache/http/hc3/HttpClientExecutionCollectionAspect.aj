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
package com.ebupt.webjoin.insight.plugin.apache.http.hc3;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.URIException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.ebupt.webjoin.insight.collection.DefaultOperationCollector;
import com.ebupt.webjoin.insight.collection.FrameBuilderHintObscuredValueMarker;
import com.ebupt.webjoin.insight.collection.OperationCollectionAspectSupport;
import com.ebupt.webjoin.insight.collection.OperationCollectionUtil;
import com.ebupt.webjoin.insight.collection.OperationCollector;
import com.ebupt.webjoin.insight.color.ColorManager.ColorParams;
import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.operation.OperationMap;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.intercept.trace.ObscuredValueMarker;

public aspect HttpClientExecutionCollectionAspect extends
		OperationCollectionAspectSupport {
	private static final InterceptConfiguration configuration = InterceptConfiguration
			.getInstance();
	// NOTE: using same value as version 4.0 to enable mutual configuration
	protected static final CollectionSettingName OBFUSCATED_HEADERS_SETTING = new CollectionSettingName(
			"obfuscated.headers", "apache.http.client",
			"Comma separated list of headers whose data requires obfuscation");

	// see RFC(s) 2616-2617
	static final String DEFAULT_OBFUSCATED_HEADERS_LIST = "Authorization,Authentication-Info,WWW-Authenticate";
	// NOTE: using a synchronized set in order to allow modification while
	// running
	static final Set<String> OBFUSCATED_HEADERS = Collections
			.synchronizedSet(new TreeSet<String>(String.CASE_INSENSITIVE_ORDER) {
				private static final long serialVersionUID = 1L;
				{
					addAll(toHeaderNameSet(DEFAULT_OBFUSCATED_HEADERS_LIST));
				}
			});

	// register a collection setting update listener to update the obfuscated
	// headers
	static {
		CollectionSettingsRegistry registry = CollectionSettingsRegistry
				.getInstance();
		registry.addListener(new CollectionSettingsUpdateListener() {
			public void incrementalUpdate(CollectionSettingName name,
					Serializable value) {
				Logger LOG = Logger
						.getLogger(HttpClientExecutionCollectionAspect.class
								.getName());
				if (OBFUSCATED_HEADERS_SETTING.equals(name)
						&& (value instanceof String)) {
					if (OBFUSCATED_HEADERS.size() > 0) { // check if replacing
															// or populating
						LOG.info("incrementalUpdate(" + name + ")"
								+ OBFUSCATED_HEADERS + " => [" + value + "]");
						OBFUSCATED_HEADERS.clear();
					}
					OBFUSCATED_HEADERS.addAll(toHeaderNameSet((String) value));
				} else if (LOG.isLoggable(Level.FINE)) {
					LOG.fine("incrementalUpdate(" + name + ")[" + value
							+ "] ignored");
				}
			}
		});
	}

	private ObscuredValueMarker obscuredMarker = new FrameBuilderHintObscuredValueMarker(
			configuration.getFrameBuilder());

	public HttpClientExecutionCollectionAspect() {
		super();
	}

	@Override
	public String getPluginName() {
		return HC3PluginRuntimeDescriptor.PLUGIN_NAME;
	}

	void setSensitiveValueMarker(ObscuredValueMarker marker) {
		this.obscuredMarker = marker;
	}

	public pointcut clientHttpExecutionFlow()
        : execution(* org.apache.commons.httpclient.HttpClient.executeMethod(HttpMethod))
       || execution(* org.apache.commons.httpclient.HttpClient.executeMethod(HostConfiguration,HttpMethod))
       || execution(* org.apache.commons.httpclient.HttpClient.executeMethod(HostConfiguration,HttpMethod,HttpState))
       ;

	public pointcut clientExecutionFlow():clientHttpExecutionFlow()&&!within(com.ebupt.webjoin..*);
	@SuppressAjWarnings({ "adviceDidNotMatch" })
	before():clientExecutionFlow()
    /* && (!cflowbelow(clientExecutionFlow()))*/
    && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart)) {
		final Operation op = enterOperation(thisJoinPointStaticPart);
		final HttpMethod method = HttpPlaceholderMethod
				.resolveHttpMethod(thisJoinPoint.getArgs());

		colorForward(new ColorParams() {
			public void setColor(String key, String value) {
				method.addRequestHeader(key, value);
			}

			public Operation getOperation() {
				return op;
			}
		});
    }
	@SuppressAjWarnings({ "adviceDidNotMatch" })
	after()returning(int statusCode):clientExecutionFlow(){
		final HttpMethod method = HttpPlaceholderMethod
				.resolveHttpMethod(thisJoinPoint.getArgs());
		DefaultOperationCollector collector =(DefaultOperationCollector) getCollector();
		Operation op = collector.builder.peek();
		System.out.println("statusCode "+statusCode);
		exitOperation(op, method, statusCode, null);
	}
	@SuppressAjWarnings({ "adviceDidNotMatch" })
	after()throwing(Throwable exp ):clientExecutionFlow(){
		final HttpMethod method = HttpPlaceholderMethod
				.resolveHttpMethod(thisJoinPoint.getArgs());
		DefaultOperationCollector collector =(DefaultOperationCollector) getCollector();
		Operation op = collector.builder.peek();
		exitOperation(op, method,
		HttpClientDefinitions.FAILED_CALL_STATUS_CODE, exp);
	}
//	@SuppressAjWarnings({ "adviceDidNotMatch" })
//	int around() throws IOException
//        : clientExecutionFlow()
//      /* && (!cflowbelow(clientExecutionFlow()))*/
//       && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart)) {
//		final Operation op = enterOperation(thisJoinPointStaticPart);
//		final HttpMethod method = HttpPlaceholderMethod
//				.resolveHttpMethod(thisJoinPoint.getArgs());
//
//		colorForward(new ColorParams() {
//			public void setColor(String key, String value) {
//				method.addRequestHeader(key, value);
//			}
//
//			public Operation getOperation() {
//				return op;
//			}
//		});

//		try {
//			int statusCode = proceed();
//			System.out.println("statusCode "+statusCode);
//			exitOperation(op, method, statusCode, null);
//			return statusCode;
//		} catch (IOException e) {
//			exitOperation(op, method,
//					HttpClientDefinitions.FAILED_CALL_STATUS_CODE, e);
//			throw e;
//		} catch (RuntimeException e) {
//			exitOperation(op, method,
//					HttpClientDefinitions.FAILED_CALL_STATUS_CODE, e);
//			throw e;
//		}
//	}

	/* -------------------------------------------------------------------- */

	Operation enterOperation(JoinPoint.StaticPart staticPart) {
		Operation op = createOperation(staticPart);
		OperationCollector collector = getCollector();
		collector.enter(op);
		return op;
	}

	Operation createOperation(JoinPoint.StaticPart staticPart) {
		return new Operation().type(HttpClientDefinitions.TYPE)
				.sourceCodeLocation(
						OperationCollectionUtil
								.getSourceCodeLocation(staticPart));
	}

	Operation exitOperation(Operation op, HttpMethod method, int statusCode,
			Throwable e) {
		fillInOperation(op, method, statusCode);

		OperationCollector collector = getCollector();
		if (e == null)
			collector.exitNormal(Integer.valueOf(statusCode));
		else
			collector.exitAbnormal(e);

		return op;
	}

	Operation fillInOperation(Operation op, HttpMethod method, int statusCode) {
		op.label(method.getName() + " " + getUri(method));
		boolean collectExtra = collectExtraInformation();
		fillInRequestDetails(op.createMap("request"), method, collectExtra);
		fillInResponseDetails(op.createMap("response"), method, statusCode,
				collectExtra);
		return op;
	}

	OperationMap fillInRequestDetails(OperationMap op, HttpMethod method,
			boolean collectExtra) {
		fillInRequestNetworkDetails(op, method, collectExtra);
		if (collectExtra) {
			fillInMethodHeaders(op.createMap("Request Headers"), method, true);
		}
		return op;
	}

	OperationMap fillInRequestNetworkDetails(OperationMap op,
			HttpMethod method, boolean collectExtra) {
		op.put("method", method.getName()).put(OperationFields.URI,
				getUri(method));

		if (collectExtra) {
			op.put("protocol", createVersionValue(method));
		}

		return op;
	}

	static String createVersionValue(HttpMethod method) {
		StatusLine line = method.getStatusLine();
		if (null != line)
			return line.getHttpVersion();
		return null;
	}

	static String getUri(HttpMethod method) {
		System.out.println("method "+method);

		try {
			System.out.println("uri "+method.getURI().toString());
			return String.valueOf(method.getURI());
		} catch (URIException e) {
			throw new RuntimeException("Failed to get URI of " + method, e);
		}
	}

	OperationMap fillInResponseDetails(OperationMap op, HttpMethod method,
			int statusCode, boolean collectExtra) {
		op.put("statusCode", statusCode);
		System.out.println("method "+method);
		System.out.println("statuscode "+statusCode);
		if (collectExtra) {
			if(method.getStatusLine()!= null)
				op.put("reasonPhrase", method.getStatusText());
			fillInMethodHeaders(op.createMap("Response Headers"), method, false);
		}

		return op;
	}

	OperationMap fillInMethodHeaders(OperationMap headers, HttpMethod method,
			boolean useRequestHeaders) {
		Header[] hdrs = useRequestHeaders ? method.getRequestHeaders() : method
				.getResponseHeaders();
		if ((hdrs == null) || (hdrs.length <= 0)) {
			return headers;
		}

		for (Header h : hdrs) {
			String name = h.getName(), value = h.getValue();
			if (OBFUSCATED_HEADERS.contains(name)) {
				obscuredMarker.markObscured(value);
			}
			headers.put(name, value);
			//OperationUtils.addNameValuePair(headers, name, value);
		}

		return headers;
	}

	static Set<String> toHeaderNameSet(String value) {
		Set<String> result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		String[] names = value.split(",");
		for (String headerName : names) {
			String trimmedValue = headerName.trim(); // in case extra whitespace
			if (trimmedValue.length() > 0) {
				result.add(trimmedValue);
			}
		}
		return result;
	}

	boolean collectExtraInformation() {
		return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration
				.getCollectionLevel());
	}
}
