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
package com.ebupt.webjoin.insight.plugin.logging;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.ebupt.webjoin.insight.collection.OperationCollector;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
 
  
/**
 * 
 */
public aspect Log4jLoggingOperationCollectionAspect extends LoggingMethodOperationCollectionAspect {
    public pointcut errorLogFlow ()
        : execution(* Category+.error(Object))
       || execution(* Category+.error(Object,Throwable))
       || execution(* Category+.fatal(Object))
       || execution(* Category+.fatal(Object,Throwable))
        ;

    public pointcut collectionPoint()
        : errorLogFlow()
      /* && (!cflowbelow(errorLogFlow()))*/
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[]    args=jp.getArgs();
        Signature   sig=jp.getSignature();
        return createOperation(jp, (Category) jp.getTarget(), sig.getName().toUpperCase(),
                               String.valueOf(args[0]), (args.length > 1) ? (Throwable) args[1] : null);
    }

    //////////////////////////////////////////////////////////////////////////

    public pointcut indirectFlow (Priority priority,Object obj)
        : (execution(* Category+.log(Priority,Object,Throwable))
       || execution(* Category+.log(Priority,Object))
       || execution(* Category+.log(String,Priority,Object,Throwable)))&&args(priority,obj)
        ;
    @SuppressAjWarnings({"adviceDidNotMatch"})
    before(Priority priority,Object obj):indirectFlow(priority,obj)
    {
    	int         pLevel=priority.toInt();
    	if ((Priority.ERROR_INT == pLevel) || (Priority.FATAL_INT == pLevel))
    	{
    		System.out.println("[before]logger error or fatal :"+thisJoinPoint);
    		 OperationCollector  collector=getCollector();
    		 Throwable exp = null;
    		 if(thisJoinPoint.getArgs().length>=3)
    			 exp = (Throwable)thisJoinPoint.getArgs()[thisJoinPoint.getArgs().length-1];
             collector.enter(createOperation(thisJoinPoint, (Category) thisJoinPoint.getTarget(),
            		 priority.toString(), String.valueOf(obj),exp));
             
    	}
    }
    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(Priority priority,Object obj):indirectFlow(priority,obj)
    {
    	int         pLevel=priority.toInt();
    	if ((Priority.ERROR_INT == pLevel) || (Priority.FATAL_INT == pLevel))
    	{
    		System.out.println("[after]logger error or fatal :"+thisJoinPoint);
    		OperationCollector  collector=getCollector();
    		collector.exitNormal();
		}
    }
    /*@SuppressAjWarnings({"adviceDidNotMatch"})
    Object around (Priority priority) : indirectFlow(priority) && (!cflowbelow(indirectFlow())) {
        Object[]    args=thisJoinPoint.getArgs();
        Level p = null;
        int         pIndex=findFirstArgumentIndex(Level.class,p, args);
    //    Level    p=(Level) args[pIndex];
        @SuppressWarnings("null")
		int         pLevel=p.toInt();
        if ((Priority.ERROR_INT == pLevel) || (Priority.FATAL_INT == pLevel)) {
            OperationCollector  collector=getCollector();
            collector.enter(createOperation(thisJoinPoint, (Category) thisJoinPoint.getTarget(),
                            p.toString(), String.valueOf(args[pIndex+1]),
                            (pIndex < (args.length - 2)) ? (Throwable) args[pIndex+2] : null));
            try
            {
                Object  returnValue=proceed();
                collector.exitNormal();
                return returnValue;
            }
            catch(RuntimeException e)
            {
                collector.exitAbnormal(e);
                throw e;
            }
        } else {    // NOT an ERROR or FATAL
            return proceed();
        }
    }
*/
    protected Operation createOperation(JoinPoint jp, Category logger, String level, String msg, Throwable t) {
        return createOperation(jp, Logger.class, level, msg, t)
                    .putAnyNonEmpty(LoggingDefinitions.NAME_ATTR, logger.getName())
                    ;
    }

  /*  int findFirstArgumentIndex (Class<?> expectedClass,Level level, Object... args) {
        for (int    index=0; index < args.length; index++) {
            Object      arg=args[index];
            Class<?>    argClass=(arg == null) ? null : arg.getClass();
          //  if(null!= arg)
          //  	System.out.println("args ["+argClass.toString()+"]"+argClass+" equal?"+(expectedClass.toString().equals(argClass.toString())));
            if ((argClass != null) && expectedClass.toString().equals(argClass.toString())) {
            	level = (Level) arg;
                return index;
            }
        }
        throw new IllegalStateException("No argument found of type " + expectedClass.getSimpleName());
    }*/
}
