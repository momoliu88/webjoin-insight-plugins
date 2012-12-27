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

package com.ebupt.webjoin.insight.plugin.tomcat.jsp;

import org.apache.jasper.compiler.Compiler;
import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.collection.AbstractOperationCollectionAspect;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;

 
public aspect JspCompilerOperationCollectionAspect extends AbstractOperationCollectionAspect {
    private static final OperationType type = OperationType.valueOf("jsp-compiler");

    public JspCompilerOperationCollectionAspect () {
    	super();
    }

    public pointcut compileExecution() 
        : execution(* Compiler+.compile(..));

    /*
     * Use cflowbelow() to avoid collecting nested calls.
     */
    public pointcut collectionPoint() 
        : compileExecution()/* && !cflowbelow(compileExecution())*/;

    @Override
	protected Operation createOperation(JoinPoint jp) {
        Compiler compiler = (Compiler) jp.getThis();
        String jspName = compiler.getCompilationContext().getJspFile();
        String compilerName = compiler.getClass().getName();
        return new Operation()
            .label("Compile JSP: " + jspName)
            .type(type).sourceCodeLocation(getSourceCodeLocation(jp))
            .put("compiler", compilerName)
            .put("jspName", jspName);
    }

    @Override
    public String getPluginName() {
        return TomcatPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
