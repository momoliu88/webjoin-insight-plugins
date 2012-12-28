package com.ebupt.webjoin.insight.intercept.generic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.Servlet;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;

public aspect HttpRequestOperationCollectionAspect extends AbstractHttpRequestOperationCollectionAspect {
//	pointcut traceOutputBuffer(ByteChunk bytechunk):execution(void org.apache.coyote.Response.doWrite(ByteChunk))&& args(bytechunk);
   // pointcut traceContentType():execution(String org.apache.coyote.Response.getContentType());
	//test
	pointcut traceRealWrite(byte[] bytes,int offset,int len):execution(void org.apache.catalina.connector.OutputBuffer.realWriteBytes(byte[], int, int)) 
	                         && args(bytes,offset,len);
	pointcut traceStaticPath():execution(void org.apache.catalina.servlets.DefaultServlet.serveResource(HttpServletRequest, HttpServletResponse, boolean));
	public pointcut collectionPoint(Request req,Response resp)
	:execution(* *.service(Request,Response))
			&&args(req,resp);

	@Override
	protected HttpResponseDetails createHttpResponseDetailsInstance() {	
		return new HttpResponseDetailsImpl();
	}
	@Override
	protected Servlet retrieveInvokedServlet(JoinPoint paramJoinPoint,
			Request paramHttpServletRequest,
			Response paramHttpServletResponse) {
		if(paramJoinPoint.getTarget() instanceof Servlet)
			return (Servlet) paramJoinPoint.getTarget();
		else return null;
	}
//	@SuppressAjWarnings("adviceDidNotMatch")
//	before(ByteChunk chunk) throws UnsupportedEncodingException:traceOutputBuffer(chunk)
//	{
//		byte [] bytes =  chunk.getBytes();
//		/*int i = 0 ;
//		for(byte oneByte:bytes)
//		{
//			if(oneByte == 0)
//				break;
//			i++;
//		}*/
//		byte [] encode = Base64.encodeBase64(bytes);
// 		setHint(FrameBuilder.HINT_HTTP_RESPONSE_BODY, new String(encode));
//	}
	@SuppressAjWarnings("adviceDidNotMatch")
	before(byte[] bytes,int offset,int len):traceRealWrite(bytes,offset,len)
	{
		byte[] subBytes = new byte[len];
		for(int i = 0; i< len;i++)
			subBytes[i]=bytes[offset+i];
		//byte [] encode = Base64.encodeBase64(subBytes);
//		System.out.println("sub string===============================");
//		System.out.println("offset "+offset+" len "+len+" total: "+bytes.length);

//		System.out.println(new String(encode));
//		System.out.println("sub string===============================");

 		setHint(FrameBuilder.HINT_HTTP_RESPONSE_BODY, subBytes);
	}
	@SuppressAjWarnings("adviceDidNotMatch")
	before():traceStaticPath()
	{
		setHint(FrameBuilder.HINT_STATIC_PATH, true);
	}
}
