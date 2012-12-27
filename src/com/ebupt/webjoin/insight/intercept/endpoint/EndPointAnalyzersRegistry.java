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

package com.ebupt.webjoin.insight.intercept.endpoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.plugin.PluginRuntimeDescriptorsRegistery;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.intercept.trace.LazyTraceFinalizer;
import com.ebupt.webjoin.insight.server.ServerName;
import com.ebupt.webjoin.insight.util.ListUtil;

 

public class EndPointAnalyzersRegistry {
    public static final String TOKEN_NAME = "X-insight-endpoint-name";
    public static final String APP_TOKEN_NAME = "X-insight-application-name";
    public static final String SERVER_TOKEN_NAME = "X-insight-server-name";
    
    private static final EndPointAnalyzersRegistry INSTANCE = new EndPointAnalyzersRegistry();
    private final Map<OperationType, List<EndPointAnalyzer>> map;
    private final EndPointAnalyzer defaultAnalyzer;
    
    private volatile boolean allRegistered;
    
    private EndPointAnalyzersRegistry() {
        this.map = new TreeMap<OperationType, List<EndPointAnalyzer>>(OperationType.BY_NAME_COMPARATOR);
        this.defaultAnalyzer = DefaultEndPointAnalyzer.getInstance();
        registerAnalyzer(TopLevelMethodEndPointAnalyzer.getInstance());
    }

    public static final EndPointAnalyzersRegistry getInstance () {
        return INSTANCE;
    }

    private void registerAllIfNeeded() {
        synchronized(defaultAnalyzer) {
            if(!allRegistered) {
                registerAll();
                allRegistered = true;
            }
        }
    }
    
    private void registerAll() {
        PluginRuntimeDescriptorsRegistery   registry=PluginRuntimeDescriptorsRegistery.getInstance();
        register(registry.getEndPointAnalyzers());
    }

    private void register(Collection<? extends EndPointAnalyzer> analyzers) {
        if (ListUtil.size(analyzers) <= 0) {
        	return;
        }

        for (EndPointAnalyzer analyzer : analyzers) {
        	registerAnalyzer(analyzer);
        }
    }

    public void registerAnalyzer(EndPointAnalyzer analyzer) {
        Collection<OperationType> types = analyzer.getOperationTypes(); 
        if (ListUtil.size(types) <= 0) {
        	return;
        }

        for (OperationType type : types) {
            registerType(analyzer, type);
        }
    }

    private void registerType(EndPointAnalyzer analyzer, OperationType type) {
        List<EndPointAnalyzer> current = getEndPointAnalyzers(type, true);
        synchronized(current) {
        	current.add(analyzer);
        }
    }
    
    public void findEndPointAnalysis(FrameBuilder builder, Frame frame, int depth) {
        registerAllIfNeeded();
        
        Operation operation = frame.getOperation();
    	// make sure all the data is there BEFORE calling the analyzer
    	LazyTraceFinalizer.callFinalizeConstructionIfPossible(operation);
        OperationType opType = operation.getType();
        List<EndPointAnalyzer> analyzers = getEndPointAnalyzers(opType, false);

        EndPointAnalysis prev = null, orig = null;
        if (analyzers != null) {
            prev = getEndPointFromHints(builder);
            orig = prev;

            synchronized(analyzers) {
	            for (EndPointAnalyzer analyzer : analyzers) {
	            	if (prev != null) {	// check if we can do better than what we have
	            		int	newScore=analyzer.getScore(frame, depth), prevScore=prev.getScore();
	            		if (newScore <= prevScore) {
	            			continue;
	            		}
	            	}

	            	EndPointAnalysis	newEndpoint=analyzer.locateEndPoint(frame, depth);
	            	if (newEndpoint == null) {
	            		continue;	// can happen because scoring does not tell us if the frame is valid...
	            	}

	            	prev = newEndpoint;
	            }
            }
            
            setEndPointOnHints(builder, prev);
        }
        
        //make sure there is always an endpoint
        if ((prev == null) && frame.isRoot()) {
            setEndPointOnHints(builder, defaultAnalyzer.locateEndPoint(frame, depth));
        }
        
        if (orig != prev) {
            setEndPointName(builder);
            
            if (orig != null) {
                Operation origOperation = orig.getSourceOperation();
                
                if (origOperation != null) {
                    origOperation.remove(OperationFields.ENDPOINT_SOURCE);
                }
                
                operation.put(OperationFields.ENDPOINT_SOURCE, true);
            }
            
        }
    }

    //for testing
    List<EndPointAnalyzer> getEndPointAnalyzers(OperationType opType, boolean createIfNotExist) {
        synchronized(map) {
        	List<EndPointAnalyzer>	list=map.get(opType);
        	if ((list == null) && createIfNotExist) {
        		list = new ArrayList<EndPointAnalyzer>();
        		map.put(opType, list);
        	}

        	return list;
        }
    }
    
    public List<EndPointAnalyzer> getEndPointAnalyzers() {
        registerAllIfNeeded();

        List<EndPointAnalyzer> analyzers=new ArrayList<EndPointAnalyzer>();
        synchronized(map) {
            for (Collection<? extends EndPointAnalyzer> analyzersList : map.values()) {
                analyzers.addAll(analyzersList);
            }
        }
        analyzers.add(defaultAnalyzer);
        
        return analyzers;
    }
    
    private void setEndPointOnHints(FrameBuilder builder, EndPointAnalysis analysis) {
        builder.setHint(TOKEN_NAME, analysis);
    }

    private EndPointAnalysis getEndPointFromHints(FrameBuilder builder) {
        return builder.getHint(TOKEN_NAME, EndPointAnalysis.class);
    }

    private void setEndPointName(FrameBuilder builder) {
        EndPointAnalysis prev = getEndPointFromHints(builder);
        
        if (prev != null) {
            EndPointPopulator populator = builder.getHint(EndPointPopulator.HINT_NAME, EndPointPopulator.class);
            
            if (populator != null) {
                populator.populateNameKey(TOKEN_NAME, prev.getEndPointName().getName());
                
                if (!populator.containsNameKey(APP_TOKEN_NAME)) {
                    ApplicationName appName = builder.getHint(FrameBuilder.HINT_APPNAME, ApplicationName.class);
                    ServerName serverName = InterceptConfiguration.getInstance().getServer();
                    
                    populator.populateNameKey(APP_TOKEN_NAME, appName.getName());
                    populator.populateNameKey(SERVER_TOKEN_NAME, serverName.getName());
                }
            }
        }
    }
    
    //for testing
    void reset() {
        synchronized(map) {
            map.clear();
        }

        registerAnalyzer(TopLevelMethodEndPointAnalyzer.getInstance());
        
        synchronized(defaultAnalyzer) {
            allRegistered = false;
        }
    }
    
}
