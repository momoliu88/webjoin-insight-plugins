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

package com.ebupt.webjoin.insight.plugin.springweb.request;


import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.collection.DefaultOperationCollector;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.plugin.springweb.AbstractSpringWebAspectSupport;
import com.ebupt.webjoin.insight.plugin.springweb.SpringWebPointcuts;
import com.ebupt.webjoin.insight.util.StringUtil;
 

public aspect WebRequestOperationCollectionAspect extends AbstractSpringWebAspectSupport {
    public WebRequestOperationCollectionAspect() {
        super(new WebRequestOperationCollector());
    }

    public pointcut collectionPoint() : SpringWebPointcuts.processWebRequest();

    @Override
    protected Operation createOperation(JoinPoint jp) {
        Object[] args = jp.getArgs();

        HttpServletRequest request = (HttpServletRequest) args[0];

        return new Operation()
            .label("Spring Web Dispatch")
            .type(OperationType.WEB_REQUEST)
            .sourceCodeLocation(getSourceCodeLocation(jp))
            .put("method", request.getMethod())
            .put(OperationFields.URI, request.getRequestURI())
            ;
    }

    static class WebRequestOperationCollector extends DefaultOperationCollector {
    	WebRequestOperationCollector () {
    		super();
    	}

        @Override
        protected void processNormalExit(Operation op) {
            op.put("error", false);
        }

        @Override
        protected void processNormalExit(Operation op, Object returnValue) {
            op.put("error", false);
        }

        @Override
        protected void processAbnormalExit(Operation op, Throwable throwable) {
            op.put("error", true)
                .put(OperationFields.EXCEPTION, StringUtil.throwableToString(throwable))
                ;
            
        }
        
    }
    
}
