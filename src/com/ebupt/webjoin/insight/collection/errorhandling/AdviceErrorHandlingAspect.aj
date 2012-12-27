/*package com.ebupt.webjoin.insight.collection.errorhandling;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.aspectj.lang.annotation.SuppressAjWarnings;

*//**
 * This aspect ensures that no exception ever escapes an advice. With this aspect, our advice should never
 * interfere normal working of the application. 
 *//*
public aspect AdviceErrorHandlingAspect {
    private static final Logger log = Logger.getLogger(AdviceErrorHandlingAspect.class.getName());

    public AdviceErrorHandlingAspect () {
    	super();
    }

    pointcut collectionAdviceExecution()
            : adviceexecution()
            && within(com.ebupt.webjoin.insight.collection..*)
            && !within(com.ebupt.webjoin.insight.collection.errorhandling..*);
    @SuppressAjWarnings("adviceNotMatched")
    void around(): collectionAdviceExecution() {
        try {
            proceed();
        } catch (Throwable throwable) {
            // log the error and report it through CollectionErrors
            log.log(Level.SEVERE, "Error swallowed in advice " + thisJoinPointStaticPart, throwable);
            CollectionErrors.markCollectionError(throwable);
        }
    }
}
*/