package com.ebupt.webjoin.insight.intercept.generic.lifecycle;

import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.aspectj.lang.annotation.SuppressAjWarnings;


public abstract aspect AbstractHttpRequestLifeCycleCollectionAspect extends ApplicationLifecycleCollectionSupport {
	public AbstractHttpRequestLifeCycleCollectionAspect()
	{
		super();
		
	}
	public abstract pointcut collectionPoint(Request req,Response resp );//:execution(void javax.servlet.GenericServlet.init(ServletConfig))&&args(config);
	public pointcut collectionPoints(Request req,Response resp ):collectionPoint(req,resp)&&!within(com.ebupt.webjoin.insight..*);

	@SuppressAjWarnings("adviceDidNotMatch")
	before(Request req,Response resp):collectionPoints(req,resp)
	{
		System.out.println("before[HTTP]ENTER:"+thisJoinPoint);
		doBeforeHttpRequest(req);
	}
	@SuppressAjWarnings("adviceDidNotMatch")
	after(Request req,Response resp) returning(Object value):collectionPoints(req,resp)
	{
		System.out.println("after[HTTP]ENTER:"+thisJoinPoint);

		doAfterHttpRequest(req,resp);
 		getCollector().exitNormal();
	}
	@SuppressAjWarnings("adviceDidNotMatch")
	after(Request req,Response resp) throwing(Throwable exp):collectionPoints(req,resp)
	{
	//	doAfterHttpRequest(req,resp);
 		getCollector().exitAbnormal(exp) ;
	}

}
