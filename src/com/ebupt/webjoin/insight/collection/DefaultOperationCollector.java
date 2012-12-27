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

package com.ebupt.webjoin.insight.collection;

import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.ltw.ClassLoaderUtils;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.util.ClassUtil;
import com.ebupt.webjoin.insight.util.StringFormatterUtils;
 
/**
 * Default implementation of {@link OperationCollector} that uses the template
 * method pattern to allow subclasses to augment behavior. This collector
 * interacts with the {@link InterceptConfiguration} to create and dispatch
 * {@link Fame}-s
 */
public class DefaultOperationCollector implements OperationCollector {
    private InterceptConfiguration interceptConfig = InterceptConfiguration.getInstance();
    public FrameBuilder builder = interceptConfig.getFrameBuilder();

    public DefaultOperationCollector() {
    	super();
    }
    
    public void setInterceptConfiguration(InterceptConfiguration config) {
        this.interceptConfig = config;
    }
    
    public void setFrameBuilder(FrameBuilder frameBuilder) {
        this.builder = frameBuilder;
    }
    
    // Final methods for entering and exiting -- these are too important to rely on subclasses to
    // do it right (instead provide process*() template methods to augment behavior)
    public final void enter(Operation op) {
        builder.enter(op);
        processEnter(op);
    }
    
    public final void exitNormal(Object returnValue) {
        _exitNormal(returnValue);
    }
    
    private Frame _exitNormal(Object returnValue) {
        Operation op = workingOperation();
        if (op != null) {
            op.put(OperationFields.RETURN_VALUE, StringFormatterUtils.formatObject(returnValue));
            processNormalExit(op, returnValue);
        }
        return exit();
    }
    
    public final void exitNormal() {
        _exitNormal();
    }
    
    private Frame _exitNormal() {
        Operation op = workingOperation();
        if (op != null) {
            op.put(OperationFields.RETURN_VALUE, "void");
            processNormalExit(op);
        }
        return exit();
    }

    public final void exitAbnormal(Throwable throwable) {
        Operation op = workingOperation();
        if (op != null) {
        	builder.setHint(FrameBuilder.HINT_HAS_EXCEPTION, 1);
            op.put(OperationFields.EXCEPTION, StringFormatterUtils.formatStackTrace(throwable));
            processAbnormalExit(op, throwable);
        }
        exit();
    }

    public void exitAndDiscard() {
        builder.setHintIfRoot(FrameBuilder.HINT_DISCARD, Boolean.TRUE);
        Frame frame = _exitNormal();
        if (builder.isFrameInTrace(frame)) {
            discard(frame);
        }
    }

    public void exitAndDiscard(Object returnValue) {
        builder.setHintIfRoot(FrameBuilder.HINT_DISCARD, Boolean.TRUE);
        Frame frame = _exitNormal(returnValue);
        if (builder.isFrameInTrace(frame)) {
            discard(frame);
        }
    }
    
    private void discard(Frame frame) {
        if (frame != null && !frame.isRoot()) {
            builder.discard(frame);
        }
    }

    protected FrameBuilder getBuilder() {
        return builder;
    }

    // Template methods for subclasses to augment behavior
    protected void processEnter(Operation op) {
        // do nothing
    }
    
    protected void processNormalExit(Operation op) {
        // do nothing
    }

    protected void processNormalExit(Operation op, Object returnValue) {
        // do nothing
    }

    protected void processAbnormalExit(Operation op, Throwable throwable) {
        // do nothing
    }
    
    private Operation workingOperation() {
        return builder.peek();
    }
    
    private Frame exit() {
    	Frame	frame=builder.peekFrame();
    	/*
    	 * If about to exit the root frame and current app. name is not set,
    	 * make another try to determine it
    	 */
    	if (builder.isCurrentRootFrame(frame)) {
    		ApplicationName	name=builder.getHint(FrameBuilder.HINT_APPNAME, ApplicationName.class);
    		if ((name == null) || ApplicationName.UNKOWN_APPLICATION.equals(name)) {
    			ClassLoader	cl=ClassUtil.getDefaultClassLoader(getClass());
    			name = ClassLoaderUtils.findApplicationName(cl);
    			builder.setHint(FrameBuilder.HINT_APPNAME, name);
    		}
    	}

        return builder.exit();
    }
}
