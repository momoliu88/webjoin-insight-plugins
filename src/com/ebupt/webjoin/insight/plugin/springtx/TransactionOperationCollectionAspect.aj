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

package com.ebupt.webjoin.insight.plugin.springtx;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import com.ebupt.webjoin.insight.collection.*;
import com.ebupt.webjoin.insight.intercept.operation.*;

 


/**
 * Aspect to monitor transactions
 * <p>
 * This aspect advises {@link PlatformTransactionManager} to monitor transaction boundaries
 * and collect transaction information. Since, it directly monitors the transaction manager,
 * this aspect will work with any configuration (programmatic usage, annotations, XML configuration)  
 *
 */
public aspect TransactionOperationCollectionAspect extends OperationCollectionAspectSupport {
    static final OperationType TYPE = OperationType.valueOf("transaction");
    
    public TransactionOperationCollectionAspect() {
        super(new TransactionOperationCollector());
    }

    @Override
    public String getPluginName() {
        return "spring-tx";
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(TransactionDefinition txDefinition) returning(TransactionStatus txStatus)
        : TransactionPointcuts.transactionBegin(txDefinition)
       && if(strategies.collect(thisAspectInstance,thisJoinPointStaticPart)) {
        // Record only if this is a new transaction 
        // (i.e. not a nested transaction with REQUIRED propagation)
        if (txStatus.isNewTransaction()) {
            getCollector().enter(createOperation(txDefinition, thisJoinPoint));
        }
    }
    
    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(TransactionStatus txStatus) returning 
        : TransactionPointcuts.transactionCommit(txStatus)
       && if(strategies.collect(thisAspectInstance,thisJoinPointStaticPart)) {
        // Record only if the transaction has completed (committed or rolled back) and is a new (top-level) tx
        if (txStatus.isNewTransaction() && txStatus.isCompleted()) {
            getCollector().exitNormal(TransactionOperationStatus.Committed);
        }
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(TransactionStatus txStatus) returning 
        : TransactionPointcuts.transactionRollback(txStatus)
       && if(strategies.collect(thisAspectInstance,thisJoinPointStaticPart)) {
        // Record only if the transaction has completed (committed or rolled back) and is a new (top-level) tx
         if (txStatus.isNewTransaction() && txStatus.isCompleted()) {
            getCollector().exitNormal(TransactionOperationStatus.RolledBack);
        }
    }
    
    protected Operation createOperation(TransactionDefinition txDefinition, JoinPoint jp) {
        Operation operation = new Operation()
            .type(TYPE)
            .sourceCodeLocation(getSourceCodeLocation(jp))
            .put("name", txDefinition.getName())
            .put("propagation", txDefinition.getPropagationBehavior())
            .put("isolation", txDefinition.getIsolationLevel())
            .put("readOnly", txDefinition.isReadOnly())
            .put("timeout", txDefinition.getTimeout());
        TransactionOperationFinalizer.register(operation);
        return operation;
    }
    
    static class TransactionOperationCollector extends DefaultOperationCollector {

        @Override
        protected void processNormalExit(Operation op, Object returnValue) {
            op.put("status", returnValue.toString());
        }
    } 
}
