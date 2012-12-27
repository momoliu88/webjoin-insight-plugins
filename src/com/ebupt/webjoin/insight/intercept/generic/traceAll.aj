package com.ebupt.webjoin.insight.intercept.generic;

public aspect traceAll {
	pointcut traceall():((execution(* *.*(..)) )&&!within(java..*)&&!within(org.apache.tomcat.util..*)&&!within(com.ebupt..*))
			;
	
	before():traceall()
	{
		System.out.println("[trace all] before "+thisJoinPoint);
	}
	after():traceall()
	{
		System.out.println("[trace all] after "+thisJoinPoint);
	}
}
