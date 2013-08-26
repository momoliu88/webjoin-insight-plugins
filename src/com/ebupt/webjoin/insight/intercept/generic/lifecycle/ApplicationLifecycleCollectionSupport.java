package com.ebupt.webjoin.insight.intercept.generic.lifecycle;

/*import java.util.logging.Level;
import java.util.logging.Logger;*/

import javax.servlet.ServletContext;

import org.apache.coyote.Request;
import org.apache.coyote.Response;

import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.collection.DefaultOperationCollector;
import com.ebupt.webjoin.insight.collection.OperationCollector;
import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.application.ApplicationMetadataCache;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationMap;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.intercept.trace.TraceId;
import com.ebupt.webjoin.insight.intercept.trace.TraceType;

public class ApplicationLifecycleCollectionSupport {
	/*
	 * private static InsightAgentClassloadingHelper helper =
	 * InsightAgentClassloadingHelper.getInstance();
	 * 
	 * private static Insight insightConfig = helper.getInsight();
	 */

	private static final boolean insightEnabled = true;// insightConfig.isInsightEnabled();
	
	private static final InterceptConfiguration interceptConfig = InterceptConfiguration
			.getInstance();
	protected final FrameBuilder frameBuilder = interceptConfig.getFrameBuilder();

	private static final ApplicationMetadataCache applicationMetaData = ApplicationMetadataCache
			.getInstance();
	private OperationCollector collector;

/*	protected final Logger log = Logger.getLogger(getClass().getName());
*/
	private static final OperationType TYPE = OperationType.APP_LIFECYCLE;
	private static final OperationType HTTPTYPE = OperationType.HTTP;
	
	public static boolean isInsightEnabled() {
		return insightEnabled;
	}

	public ApplicationLifecycleCollectionSupport() {
	/*	if (this.log.isLoggable(Level.FINE)) {
			this.log.fine("*********************************************************");
			this.log.fine("* " + getClass().getSimpleName()
					+ " constructed via " + getClass().getClassLoader());
			this.log.fine("*********************************************************");
		}*/
		this.collector =  new DefaultOperationCollector();

	}
	public OperationCollector getCollector()
	{
		return this.collector;
	}
	private String makeLabel(Request req) {
		StringBuffer label = new StringBuffer(req.method().getString());
		label.append(" ").append(req.requestURI().toString());
		if (req.queryString() != null && req.queryString().toString() != null)
			label.append("?").append(req.queryString().toString());
		return label.toString();
	}

	protected void doBeforeHttpRequest(Request req) {
		Operation op = new Operation().type(HTTPTYPE).label(makeLabel(req));
	//	this.frameBuilder.setHint("trace-type", TraceType.HTTP);
	//	this.frameBuilder.enter(op);
		getCollector().enter(op);
	}

	protected void doAfterHttpRequest(Request req, Response resp) {
		Operation op = this.frameBuilder.peek();
		ApplicationName appName = ApplicationName.UNKOWN_APPLICATION;
		if(resp != null)
		{
			op.put("statusCode", resp.getStatus());
			if(resp.getContentLength() >= 0)
				op.put("contentLength", resp.getContentLength());
			Object responseInfo = this.frameBuilder.getHint(FrameBuilder.HINT_HTTP_RESPONSE);
			//modify some value
			if(null !=responseInfo && responseInfo instanceof OperationMap)
			{
				OperationMap respMap = (OperationMap)responseInfo;
				respMap.put("statusCode",resp.getStatus());
			//	respMap.put("contentLength", resp.getContentLength());
			}
		}
		if(req != null)
			appName = ApplicationName.valueOf(makeLabel(req));
		this.frameBuilder.setHintIfRoot(FrameBuilder.HINT_TRACETYPE, TraceType.HTTP);
		this.frameBuilder.setHintIfRoot(FrameBuilder.HINT_APPNAME, appName);
		this.frameBuilder.setHintIfRoot(FrameBuilder.HINT_TRACEID,
				TraceId.valueOf());
		//getCollector().exitNormal();
	}

	protected void doBeforeContextInitialization(ServletContext ctx) {
		Operation op = new Operation().type(TYPE).label(
				"Servlet:" + ctx.getContextPath());
		System.out.println("before context init :"+op.getLabel());
  		this.frameBuilder.enter(op);
	}

	protected void doAfterContextInitialization(ServletContext ctx) {
		String ctxName = "Servlet:" + ctx.getContextPath();
		ApplicationName appName = ApplicationName.valueOf(ctxName);
		System.out.println("lifecycle done [" + Thread.currentThread().getId()
				+ "]");
		System.out.println("set app "+appName);
		this.frameBuilder.setHintIfRoot(FrameBuilder.HINT_APPNAME, appName);
		this.frameBuilder.setHintIfRoot(FrameBuilder.HINT_TRACEID,
				TraceId.valueOf());
		this.frameBuilder.setHintIfRoot(FrameBuilder.HINT_TRACETYPE, TraceType.LIFECYLE);

		this.frameBuilder.exit();
		applicationMetaData.setLabel(appName, ctxName);
		System.out.println("after context init :"+ctxName);
		System.out.println("depth "+this.frameBuilder.getdepth());

	}

}