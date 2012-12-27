package com.ebupt.webjoin.insight.intercept.generic.lifecycle;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
 
public aspect ApplicationLifecycleCollectionAspect extends AbstractApplicationLifecycleCollectionAspect{
 	pointcut init(ServletContextEvent event): execution(* ServletContextListener.contextInitialized(ServletContextEvent))&&args(event);
 	pointcut destory(ServletContextEvent event)
 	:execution(* ServletContextListener.contextDestroyed(ServletContextEvent))&&args(event);

	public  pointcut collectionPoint(ServletContextEvent event)
		:init(event)||destory(event);
}
