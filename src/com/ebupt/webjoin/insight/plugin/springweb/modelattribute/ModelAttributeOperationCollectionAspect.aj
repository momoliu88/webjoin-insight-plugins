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

package com.ebupt.webjoin.insight.plugin.springweb.modelattribute;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Conventions;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.ebupt.webjoin.insight.collection.DefaultOperationCollector;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.plugin.springweb.AbstractSpringWebAspectSupport;
import com.ebupt.webjoin.insight.plugin.springweb.ControllerPointcuts;
import com.ebupt.webjoin.insight.util.StringFormatterUtils;
import com.ebupt.webjoin.insight.util.StringUtil;
 

public aspect ModelAttributeOperationCollectionAspect extends AbstractSpringWebAspectSupport {
    
    private static final OperationType TYPE = OperationType.valueOf("model_attribute");
    
    public pointcut collectionPoint() : ControllerPointcuts.modelAttributeRetrieval();
    
    public ModelAttributeOperationCollectionAspect() {
        super(new ModelAttributeOperationCollector());
    }
    
    @Override
    protected Operation createOperation(JoinPoint jp) {
        MethodSignature sig = (MethodSignature)jp.getSignature();
        String methodString = sig.getDeclaringType().getSimpleName() + "#" + sig.getName();
        return new Operation()
            .label("@ModelAttribute " + methodString)
            .type(TYPE)
            .sourceCodeLocation(getSourceCodeLocation(jp))
            .put("modelAttributeName", extractModelAttributeName(jp));
    }

    private String extractModelAttributeName(JoinPoint jp) {
        Method method = ((MethodSignature)jp.getSignature()).getMethod();
        ModelAttribute ma = method.getAnnotation(ModelAttribute.class);
        String modelAttrName = (ma == null) ? null : ma.value();
        if(!StringUtil.isEmpty(modelAttrName)) {
            return modelAttrName;
        }

        return Conventions.getVariableName(((MethodSignature)jp.getSignature()).getMethod());
    }
    
    static class ModelAttributeOperationCollector extends DefaultOperationCollector {
    	ModelAttributeOperationCollector  () {
    		super();
    	}

        @Override
        protected void processNormalExit(Operation op, Object returnValue) {
            op.put("value", StringFormatterUtils.formatObject(returnValue));
        }
        
    }
}
