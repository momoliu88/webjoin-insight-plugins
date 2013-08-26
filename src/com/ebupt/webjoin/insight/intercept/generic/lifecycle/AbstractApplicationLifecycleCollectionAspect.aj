package com.ebupt.webjoin.insight.intercept.generic.lifecycle;


 
import javax.servlet.ServletContextEvent;

import org.aspectj.lang.annotation.SuppressAjWarnings;

public abstract aspect AbstractApplicationLifecycleCollectionAspect extends ApplicationLifecycleCollectionSupport{
	
	public AbstractApplicationLifecycleCollectionAspect()
	{
		super();
		
	}
	public abstract pointcut collectionPoint(ServletContextEvent event);//:execution(void javax.servlet.GenericServlet.init(ServletConfig))&&args(config);
	public pointcut collectionPoints(ServletContextEvent event):collectionPoint(event)&&!within(com.ebupt.webjoin.insight..*);

	@SuppressAjWarnings("adviceDidNotMatch")
	before(ServletContextEvent event ):collectionPoint(event)
	{	
		doBeforeContextInitialization(event.getServletContext());
	}
	@SuppressAjWarnings("adviceDidNotMatch")
	after(ServletContextEvent event ):collectionPoint(event)
	{
		doAfterContextInitialization(event.getServletContext());
	}
}
