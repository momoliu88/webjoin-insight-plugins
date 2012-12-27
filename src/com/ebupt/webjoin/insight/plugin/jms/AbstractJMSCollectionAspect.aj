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
package com.ebupt.webjoin.insight.plugin.jms;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.collection.*;
import com.ebupt.webjoin.insight.intercept.*;
import com.ebupt.webjoin.insight.intercept.operation.*;
import com.ebupt.webjoin.insight.intercept.plugin.*;
import com.ebupt.webjoin.insight.intercept.trace.*;
import com.ebupt.webjoin.insight.util.*;



public abstract aspect AbstractJMSCollectionAspect extends OperationCollectionAspectSupport {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();

    public static final CollectionSettingName    OBFUSCATED_HEADERS_SETTING =
            new CollectionSettingName("obfuscated.headers", JmsPluginRuntimeDescriptor.PLUGIN_NAME, "Comma separated list of headers whose data requires obfuscation");
    public static final CollectionSettingName    OBFUSCATED_PROPERTIES_SETTING =
            new CollectionSettingName("obfuscated.properties", JmsPluginRuntimeDescriptor.PLUGIN_NAME, "Comma separated list of properties whose data requires obfuscation");

    // NOTE: using a synchronized set in order to allow modification while running
    static final Set<String>    OBFUSCATED_HEADERS=Collections.synchronizedSet(new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));
    static final Set<String>    OBFUSCATED_PROPERTIES=Collections.synchronizedSet(new TreeSet<String>(String.CASE_INSENSITIVE_ORDER));
    // register a collection setting update listener to update the obfuscated headers
    static {
        CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
        registry.addListener(new CollectionSettingsUpdateListener() {
            	public void incrementalUpdate (CollectionSettingName name, Serializable value) {
            		if (OBFUSCATED_HEADERS_SETTING.equals(name)) {
            			updateObscuredSettings(name, value, OBFUSCATED_HEADERS);
            		} else if (OBFUSCATED_PROPERTIES_SETTING.equals(name)) {
            			updateObscuredSettings(name, value, OBFUSCATED_PROPERTIES);
            		}
            	}
        	});
    }

    static void updateObscuredSettings (CollectionSettingName name, Serializable value, Collection<String> nameSet) {
    	if (!(value instanceof String)) {
    		return;
    	}

    	Logger	LOG=Logger.getLogger(AbstractJMSCollectionAspect.class.getName());
    	if (nameSet.size() > 0) {
    		LOG.info("updateObscuredSettings(" + name + ")" + nameSet + " => [" + value + "]");
    		nameSet.clear();
    	}
    	
    	nameSet.addAll(toHeaderNameSet((String) value));
    }

    static Set<String> toHeaderNameSet (String value) {
        Set<String> result=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        String[]    names=value.split(",");
        for (String headerName : names) {
            String   trimmedValue=headerName.trim(); // in case extra whitespace
            if (trimmedValue.length() > 0) {
                result.add(trimmedValue);
            }
        }
        return result;
    }

	protected final JMSPluginOperationType optype;
    protected ObscuredValueMarker obscuredMarker =
            new FrameBuilderHintObscuredValueMarker(configuration.getFrameBuilder());

    protected AbstractJMSCollectionAspect (JMSPluginOperationType type) {
    	if ((optype=type) == null) {
    		throw new IllegalStateException("No operation type specified");
    	}
    }

    ObscuredValueMarker getSensitiveValueMarker () {
    	return this.obscuredMarker;
    }

    void setSensitiveValueMarker(ObscuredValueMarker marker) {
        this.obscuredMarker = marker;
    }
    
    Operation createOperation(JoinPoint jp) {
        Operation op = new Operation();
        op.type(optype.getOperationType());
        op.label(optype.getLabel());
        op.sourceCodeLocation(getSourceCodeLocation(jp));
        
        return op;
    }

    Operation applyDestinationData(Message message, Operation op) {
        try {
            return applyDestinationData(message.getJMSDestination(), op);
        } catch (JMSException e) {
            markException("applyDestinationData[getDest]", e);
            return op;
        }
    }
    
    Operation applyDestinationData(Destination dest, Operation op) {
        try {
            DestinationType destinationType = JMSPluginUtils.getDestinationType(dest);
            String destinationName = JMSPluginUtils.getDestinationName(dest, destinationType);
            return applyDestinationData(op, destinationType, destinationName);
        } catch (JMSException e) {
            markException("applyDestinationData[fillOp]", e);
            return op;
        }
    }
    
    Operation applyMessageData(Message message, Operation op) {
        try {
            JMSPluginUtils.extractMessageTypeAttributes(op, message);
        } catch(JMSException e) {
        	markException("extractMessageTypeAttributes", e);
        }
        
        try {
            JMSPluginUtils.extractMessageHeaders(op, message, getSensitiveValueMarker(), OBFUSCATED_HEADERS);
        } catch(JMSException e) {
        	markException("extractMessageHeaders", e);
        }
        
        try {
        	JMSPluginUtils.extractMessageProperties(op, message, getSensitiveValueMarker(), OBFUSCATED_PROPERTIES);
        } catch (JMSException e) {
        	markException("extractMessageProperties", e);
        }

        return op;
    }

    // returns a Map of the obscured keys and values
    Map<String,Object> obscureValues (OperationMap valuesMap, CollectionSettingName settingName, Collection<String> nameSet) {
    	if ((ListUtil.size(nameSet) <= 0) || (valuesMap.size() <= 0)) {
    		return Collections.emptyMap();	// nothing to obscure
    	}

    	Map<String,Object>	result=null;
    	for (String key : valuesMap.keySet()) {
    		if (!nameSet.contains(key)) {
    			continue;
    		}

    		Object	value=valuesMap.get(key);
    		obscuredMarker.markObscured(value);
    		
    		if (result == null) {
    			result = new TreeMap<String, Object>();
    		}
    		
    		result.put(key, value);
    	}

    	if (result == null) {
    		return Collections.emptyMap();
    	} else {
    		return result;
    	}
    }

    static Operation applyDestinationData (Operation op, DestinationType destinationType, String destinationName) {
        op.put(OperationFields.CLASS_NAME, destinationType.name());
        op.put(OperationFields.METHOD_SIGNATURE, destinationName);
        
        op.put("destinationType", destinationType.name());
        op.put("destinationName", destinationName);
        return op;
    }

    @Override
    public boolean isEndpoint() {
    	return true;
    }

    @Override
    public String getPluginName() {
        return JmsPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
