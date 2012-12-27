package com.ebupt.webjoin.insight.intercept.generic.lifecycle;

import org.apache.coyote.Request;
import org.apache.coyote.Response;

 
public aspect HttpRequestLifecycleCollectionAspect extends AbstractHttpRequestLifeCycleCollectionAspect{
 	pointcut httpRequest(Request req,Response resp):execution(* org.apache.coyote.Adapter.service(Request , Response ))&&args(req,resp); //execution(void org.apache.tomcat.util.net.JIoEndpoint.SocketProcessor.run());
 	public  pointcut collectionPoint(Request req,Response resp ):httpRequest(req,resp);
	
}
