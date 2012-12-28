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

package com.ebupt.webjoin.insight.intercept;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.Insight;
import com.ebupt.webjoin.insight.application.ApplicationName;
//import com.ebupt.webjoin.insight.intercept.endpoint.EndPointAnalysis;
//import com.ebupt.webjoin.insight.intercept.endpoint.EndPointAnalyzersRegistry;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.operation.OperationList;
import com.ebupt.webjoin.insight.intercept.operation.OperationMap;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.operation.SourceCodeLocation;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;
import com.ebupt.webjoin.insight.intercept.trace.EmptyObscuredValueRegistry;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.intercept.trace.FrameId;
import com.ebupt.webjoin.insight.intercept.trace.NullFrameBuilderCallback;
import com.ebupt.webjoin.insight.intercept.trace.ObscuredValueRegistry;
import com.ebupt.webjoin.insight.intercept.trace.SimpleFrame;
import com.ebupt.webjoin.insight.intercept.trace.StopWatchFrame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.intercept.trace.TraceId;
import com.ebupt.webjoin.insight.intercept.trace.TraceType;
import com.ebupt.webjoin.insight.intercept.util.time.TimeRange;
import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.ClassUtil;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.StringFormatterUtils;
import com.ebupt.webjoin.insight.util.StringUtil;



/**
 * Package-private helper class which handles callbacks when the {@link FrameBuilder} 
 * finishes building a full frame stack.
 *
 * This callback handles dispatching the trace within the {@link InterceptConfiguration}
 */
public class TraceDispatchCallback extends NullFrameBuilderCallback {
	/**
	 * The (global) {@link CollectionSettingName} used to control whether
	 * extra information is generated for {@link ApplicationName#UNKOWN_APPLICATION}-s.
	 * Default: <code>false</code>
	 */
	public static final CollectionSettingName	ELABORATE_UNKNOWN_APPS=new CollectionSettingName("elaborate.unknown.apps");

	/**
	 * The special {@link OperationType} used to mark the generated synthetic frame
	 * {@link Operation} for unknown applications
	 */
	public static final OperationType	ELABORATE_UNKNOWN_APPOP=OperationType.valueOf("elaborate-unknown-app");

	/**
	 * The {@link FrameId} assigned to the synthetic frame generated for unknown applications
	 */
	public static final FrameId	ELABORATE_UNKNOWN_APP_FRAMEID=FrameId.valueOf(Integer.MAX_VALUE);

	/**
	 * The label assigned to the synthetic frame {@link Operation}
	 */
	public static final String ELABORATE_UNKNOWN_LABEL="Unknown application call stack data";

    private final InterceptConfiguration intercept;

    public TraceDispatchCallback(InterceptConfiguration interceptConfig) {
        this.intercept = interceptConfig;
    }

	@Override
    public void exitRootFrame(Frame rootFrame, Map<String, Object> hints) {
        if (hasAbortedHint(hints)) 
            return;
        
        ApplicationName	appName=getAppName(hints);
        if (ApplicationName.UNKOWN_APPLICATION.equals(appName)) {
        	handleUnknownApplication(rootFrame, hints);
        }

        Trace trace=Trace.newInstance(intercept.getServer(), appName, getTraceId(hints),getTraceType(hints), rootFrame,hints);
        if (hasSensitiveHint(hints)) {
            trace.markSensitive();
        }

        if (hasMinimalHint(hints)) {
            trace.markMinimalTrace();
        }
//        
//        if (hasMandatoryHint(hints)) {
//            trace.markMandatory();
//        }

        trace.setSensitiveValues(getSensitiveValues(hints));

        intercept.dispatchTrace(trace);
    }

    /**
     * Called by {@link TraceDispatchCallback#exitRootFrame(Frame, Map)} when
     * an associated {@link ApplicationName#UNKOWN_APPLICATION} is detected.
     * If enabled, then generates a <U>synthetic</U> child of the root containing
     * information about the class loader and the stack trace that led to the root
     * @param rootFrame The root {@link Frame}
     * @param hints The associated hints {@link Map}
     * @return The generated <U>synthetic</U> child of the root - or
     * <code>null</code> if feature is disabled
     * @see #ELABORATE_UNKNOWN_APPS
     */
    protected Frame handleUnknownApplication (Frame rootFrame, Map<String,?> hints) {
        CollectionSettingsRegistry	registry=CollectionSettingsRegistry.getInstance();
        Serializable				value=registry.get(ELABORATE_UNKNOWN_APPS);
        if ((value == null) || (!CollectionSettingsRegistry.getBooleanSettingValue(value))) {
        	return null;
        }

		Logger	LOG=Logger.getLogger(getClass().getName());
		/*
		 * NOTE: we currently support only StopWatchFrame(s) because this is what the
		 * SimpleFrameBuilder uses. If need be, we can also support SimpleFrame(s),
		 * but then we will need to export an "addChild" method similar to StopWatchFrame.
		 */
		if (!(rootFrame instanceof StopWatchFrame)) {
			LOG.warning("exitRootFrame(" + rootFrame + ") not a StopWatchFrame");
			return null;
		}

    	ClassLoader				cl=ClassUtil.getDefaultClassLoader(getClass());
    	List<StackTraceElement>	stackTrace=findOriginalCallLocation(new Throwable().fillInStackTrace().getStackTrace());
    	Operation				op=new Operation().type(ELABORATE_UNKNOWN_APPOP).label(ELABORATE_UNKNOWN_LABEL);

    	LOG.warning("handleUnknownApplication(class-loader): " + cl);
    	if (ListUtil.size(stackTrace) > 0) {
    		StackTraceElement	root=stackTrace.get(0);
    		op.sourceCodeLocation(new SourceCodeLocation(root));

    		LOG.warning("handleUnknownApplication(root): " + root);
    		if (LOG.isLoggable(Level.FINE)) {
    			for (StackTraceElement elem : stackTrace) {
    				LOG.fine(" ==> handleUnknownApplication: " + elem);
    			}
        	}
    	}

    	encodeClassLoaderInfo(op.createMap("classLoader"), cl);
    	encodeStackTrace(op.createList("stackTrace"), stackTrace);

    	Frame	extraFrame=new SimpleFrame(ELABORATE_UNKNOWN_APP_FRAMEID, rootFrame, op, createExtraFrameTimeRange(rootFrame), Collections.<Frame>emptyList());
    	((StopWatchFrame) rootFrame).addChild(extraFrame);
    	return extraFrame;
    }

    protected TimeRange createExtraFrameTimeRange (Frame rootFrame) {
    	TimeRange	rootRange=rootFrame.getRange();
    	long		rootStart=rootRange.getStart();
    	return new TimeRange(rootStart + 1L, rootStart + 2L);
    }

    protected OperationList encodeStackTrace (OperationList list, Collection<? extends StackTraceElement> stackTrace) {
    	if (ListUtil.size(stackTrace) <= 0) {
    		return list;
    	}

    	for (StackTraceElement elem : stackTrace) {
    		OperationMap	map=list.createMap();
    		map.put(OperationFields.CLASS_NAME, elem.getClassName())
    		   .put(OperationFields.METHOD_NAME,  elem.getMethodName())
    		   .put(OperationFields.LINE_NUMBER, elem.getLineNumber())
    		   ;
    	}

    	return list;
    }

    protected OperationMap encodeClassLoaderInfo (OperationMap map, ClassLoader cl) {
    	if (cl == null) {
    		return map;
    	}

    	String	clData=StringUtil.trimWithEllipsis(String.valueOf(cl), StringFormatterUtils.MAX_PARAM_LENGTH);
    	return map.put(OperationFields.CLASS_NAME, cl.getClass().getName())
    			  .put(OperationFields.METHOD_SIGNATURE, clData)
    			  ;
    }

    protected List<StackTraceElement> findOriginalCallLocation (StackTraceElement ... stackTrace) {
    	if (ArrayUtil.length(stackTrace) <= 0) {
    		return Collections.emptyList();
    	}

    	Package	pkg=Insight.class.getPackage();
    	String	pkgName=pkg.getName();
    	for (int	index=0; index < stackTrace.length; index++) {
    		StackTraceElement	elem=stackTrace[index];
    		String				className=elem.getClassName();
    		if (className.startsWith(pkgName)) {
    			continue;
    		}

    		return ArrayUtil.indexRangeToList(index, stackTrace.length, stackTrace);
    	}

    	// this point is reached if no call was "outside" this package
    	return Arrays.asList(stackTrace);
    }

    private ApplicationName getAppName(Map<String,?> hints) {
        ApplicationName appName = (ApplicationName)hints.get(FrameBuilder.HINT_APPNAME);
        if (appName == null) {
            appName = ApplicationName.UNKOWN_APPLICATION;
        }
        return appName;
    }
    
    private TraceId getTraceId(Map<String,?> hints) {
        TraceId traceId = (TraceId)hints.get(FrameBuilder.HINT_TRACEID);
        if (traceId == null) {
            traceId = TraceId.valueOf()/*intercept.generateNextTraceId()*/;
        }
        return traceId;
    }
    private TraceType getTraceType(Map<String,?>hints)
    {
    	TraceType type = (TraceType)hints.get(FrameBuilder.HINT_TRACETYPE);
    	return type;
    }
    private boolean hasAbortedHint(Map<String,?> hints) {
        return _hasHint(FrameBuilder.HINT_ABORTED, hints);
    }
    
    private boolean hasSensitiveHint(Map<String,?> hints) {
        return _hasHint(FrameBuilder.HINT_SENSITIVE, hints);
    }
    
//    private boolean hasMandatoryHint(Map<String,?> hints) {
//        return _hasHint(FrameBuilder.HINT_MANDATORY, hints);
//    }

    private boolean hasMinimalHint(Map<String,?> hints) {
        return _hasHint(FrameBuilder.HINT_COLLECT_ONLY_ENDPOINTS, hints);
    }

    private boolean _hasHint(String hintName, Map<String,?> hints) {
        Object hintVal = hints.get(hintName);
        return Boolean.TRUE.equals(hintVal);
    }

    private ObscuredValueRegistry getSensitiveValues(Map<String, Object> hints) {
        ObscuredValueRegistry sensitiveValues = (ObscuredValueRegistry)hints.get(FrameBuilder.HINT_OBSCURED_REGISTRY);
        if (sensitiveValues == null) {
            return EmptyObscuredValueRegistry.getInstance();
        }
        return sensitiveValues;
    }

    public List<FrameBuilderEvent> listensTo() {
        return Arrays.asList(FrameBuilderEvent.ROOT_EXIT);
    }

}
