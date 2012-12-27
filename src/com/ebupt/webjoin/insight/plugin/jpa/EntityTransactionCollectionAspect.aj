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

package com.ebupt.webjoin.insight.plugin.jpa;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;

import com.ebupt.webjoin.insight.collection.AbstractOperationCollectionAspect;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;

import javax.persistence.EntityTransaction;


/**
 * 
 */
public aspect EntityTransactionCollectionAspect extends AbstractOperationCollectionAspect {
    public EntityTransactionCollectionAspect () {
        super();
    }

    @Override
    public String getPluginName() {
        return JpaPluginRuntimeDescriptor.PLUGIN_NAME;
    }

    public pointcut collectionPoint()
        : execution(* EntityTransaction+.begin())
       || execution(* EntityTransaction+.commit())
       || execution(* EntityTransaction+.rollback())
        ;

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Signature   sig=jp.getSignature();
        String      actionName=sig.getName();
        return new Operation().type(JpaDefinitions.TX_ENTITY)
                              .label("Transaction " + actionName)
                              .sourceCodeLocation(getSourceCodeLocation(jp))
                              .put(OperationFields.METHOD_NAME, actionName)
                              ;
    }
}
