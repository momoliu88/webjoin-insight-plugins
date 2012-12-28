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

package com.ebupt.webjoin.insight.intercept.trace;

import com.ebupt.webjoin.insight.PropertiesReader;
import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.intercept.endpoint.EndPointAnalysis;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.operation.OperationMap;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.util.time.*;
import com.ebupt.webjoin.insight.json.InsightJsonObject;
import com.ebupt.webjoin.insight.resource.ResourceKey;
import com.ebupt.webjoin.insight.server.ServerName;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.MapUtil;
import com.ebupt.webjoin.insight.util.MathUtil;
import com.ebupt.webjoin.insight.util.ObjectUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import  org.apache.commons.codec.binary.Base64;

 

/**
 * A Trace is a hierarchical list of frames which were collected at
 * a certain time, on a certain server.
 */
public class Trace implements Serializable, TraceInterface {
    public static final ServerName UNKNOWN_SERVER = ServerName.valueOf("UNKNOWN");
    //public static final String  OPERATION_MAP_NAME = "ExtraTraceData";
    public static final String  ANALYSIS_FIELD = "endpoint",
                                MANDATORY_FIELD = "mandatory";
	/**
	 * The {@link OperationFields} used by {@link #getExternalFrames()} to
	 * determine if a {@link Frame} carries external resource information
	 */
	public static final List<String>	EXTERNAL_KEYS=
			Collections.unmodifiableList(Arrays.asList(OperationFields.OPERATION_KEY, OperationFields.PARENT_KEY));
    private static final long serialVersionUID = 1527618042489712761L;

    private ServerName server;
    private ApplicationName appName;
    private long date;
    private TraceId id;
    private String userId;
    private Map<String,Object> hints;
	
	private TraceType type;
    private int pid;
   
	private Frame root;
    private boolean sensitive;
    private transient Date dateValue;
    private transient boolean minimalTrace;
    private transient ObscuredValueRegistry sensitiveValues = EmptyObscuredValueRegistry.getInstance();
//    private transient volatile EndPointAnalysis endpoint;
//    private transient volatile Boolean mandatory;

    /**
     * Constructor and Setter Methods default scope to allow for mutability in deserialization
     */
    Trace() {
        super();
    }
    public Trace(ServerName serverName, ApplicationName applicationName, Date traceDate, TraceId traceId, Frame rootFrame,Map<String,Object>hints) 
    {
        this(serverName, applicationName, traceDate, traceId,TraceType.SIMPLE, rootFrame, hints);
    }
//    public Trace(ServerName serverName, ApplicationName applicationName, Date traceDate, TraceId traceId,TraceType type, Frame rootFrame,Map<String,Object>hints) 
//    {
//        this(serverName, applicationName, traceDate, traceId,type, rootFrame, hints);
//    }
    public Trace(ServerName serverName, ApplicationName applicationName, Date traceDate, TraceId traceId, Frame rootFrame) {
        this(serverName, applicationName, traceDate, traceId,TraceType.SIMPLE, rootFrame, null);
    }

    public Trace(ServerName serverName, ApplicationName applicationName, Date traceDate, TraceId traceId,TraceType traceType, Frame rootFrame,Map<String,Object>hints) {
        if (traceDate.before(TimeUtil.EPOCH)) {
            throw new IllegalArgumentException("Date cannot be before epoch");
        }
        this.hints = hints;
        this.server = serverName;
        this.appName = applicationName;
        this.date = traceDate.getTime();
        this.id = traceId;
        this.type = traceType;
        this.root = rootFrame;
        String userIdFromProp = PropertiesReader.getProps().getProperty("user_email");
        this.userId = userIdFromProp==null?"unknown":userIdFromProp;
        this.sensitive = false;
//        this.endpoint = endpointAnalysis;
        this.pid = Integer.valueOf(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);

      //  updateFrameHints(rootFrame);
    }
    public Map<String, Object> getHints() {
		return hints;
	}
    public int getPid() {
		return pid;
	}
    public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
    public TraceId getId() {
        return id;
    }
    public TraceType getType()
    {
    	return this.type;
    }
    public ServerName getServer() {
        return server;
    }

    public void setServer(ServerName serverName) {
        this.server = serverName;
    }

    public String getLabel() {
        return root.getOperation().getLabel();
    }

    public TimeRange getRange() {
        return root.getRange();
    }

    public Frame getRootFrame() {
        return root;
    }

    // Getter matches field name for serialization support
    public ApplicationName getAppName() {
        return appName;
    }

    // Setter matches field name for deserialization support
    public void setAppName(ApplicationName name) {
        appName = name;
    }

    public Date getDate() {
        if (dateValue == null) {
            dateValue = new ImmutableDate(getTimestamp());
        }
        
        return dateValue;
    }

    public long getTimestamp () {
        return date;
    }

//    public EndPointAnalysis getEndpoint() {
//        if (endpoint == null) {
//            endpoint = resolveEndpointValue(getRootFrame());
//        }
//
//        return endpoint;
//    }

//    public void setEndpoint(EndPointAnalysis endpointAnalysis) {
//        endpoint = endpointAnalysis;
//     //   updateFrameHints(getRootFrame());
//    }
    
//    public boolean isMandatory() {
//        if (mandatory == null) {
//            mandatory = resolveMandatoryValue(getRootFrame());
//        }
//
//        return (mandatory != null) && mandatory.booleanValue();
//    }
//    
//    public void markMandatory() {
//        setMandatory(true);
//    }

    /**
     * Indicates if this trace is known to contain sensitive data that must
     * not be persisted or transmitted beyond the current host.
     */
    public boolean isSensitive() {
        return sensitive;
    }

    /**
     * Indicates that this trace is known to contains sensitive data.
     */
    public void markSensitive() {
        sensitive = true;
    }

    /**
     * Is this trace "minimal" - that is; does it contain only
     * the traces required for trace processing
     */
    public boolean isMinimalTrace() {
        return minimalTrace;
    }

    /**
     * Mark this trace as minimal
     */
    public void markMinimalTrace() {
        minimalTrace = true;
    }

    /**
     * Set the registry of sensitive values associated with this trace
     */
    public void setSensitiveValues(ObscuredValueRegistry values) {
        sensitiveValues = values;
    }

    /**
     * Returns a registry of any values which are considered sensitive
     */
    public ObscuredValueRegistry getSensitiveValues() {
        return sensitiveValues;
    }

    /**
     * Get a Frame within the trace, by id.
     * @return null if the frame does not exist.
     */
    public Frame getFrame(FrameId frameId) {
        return getFrame(frameId, getRootFrame());
    }

    /**
     * Navigate the hierarchy of frames (starting at the root and
     * descending to the children), looking for the first frame
     * which contains an operation of the given type.
     * 
     * @return null if no such frame could be found.
     */
    public Frame getFirstFrameOfType(OperationType opType) {
        return getFrame(opType, getRootFrame());
    }

    void setDate(long value) {
        setTimestamp(value);
    }

    void setTimestamp (long value) {
        date = value;

        if (dateValue != null) {    // force lazy re-initialization
            dateValue = null;
        }
    }

    void setId(TraceId traceId) {
        id = traceId;
    }

    void setRoot(Frame rootFrame) {
        root = rootFrame;
 //       updateFrameHints(rootFrame);
    }

    void setSensitive(boolean flagValue) {
        this.sensitive = flagValue;
    }
//    
//    void setMandatory(boolean flagValue) {
//        mandatory = Boolean.valueOf(flagValue);
//        updateFrameHints(getRootFrame());
//    }

//    OperationMap updateFrameHints (Frame frame) {
//        return (frame == null) ? null : updateFrameHints(frame.getOperation());
//    }

//    OperationMap updateFrameHints (Operation op) {
//        if (op == null) {
//            return null;
//        }
//
//        OperationMap    map;
//        synchronized(op) {
//            if ((map=op.get(OPERATION_MAP_NAME, OperationMap.class)) == null) {
//                map = op.createMap(OPERATION_MAP_NAME);
//            }
//        }
//        
//        return updateFrameHints(map);
//    }
//
//    OperationMap updateFrameHints (OperationMap map) {
//        if (map == null) {
//            return null;
//        }
//
//        synchronized(map) {
//            map.put(MANDATORY_FIELD, (mandatory != null) && mandatory.booleanValue());
//            updateEndPointAnalysis(map, endpoint);
//        }
//        return map;
//    }

    static final OperationMap updateEndPointAnalysis (OperationMap map, EndPointAnalysis endpoint) {
        OperationMap    analysisMap=map.get(ANALYSIS_FIELD, OperationMap.class);
        if (endpoint == null) {
            if (analysisMap != null) {  // mark the data as ignored
                EndPointAnalysis.setIgnored(analysisMap, true);
            }
        } else {
            if (analysisMap != null) {  // reverse the previous setting (if any)
                EndPointAnalysis.setIgnored(analysisMap, false);
            } else {
                analysisMap = map.createMap(ANALYSIS_FIELD);
            }

            endpoint.encode(analysisMap);
        }
        
        return analysisMap;
    }

//    static final Boolean resolveMandatoryValue (Frame frame) {
//        return resolveMandatoryValue(getEncodedMap(frame));
//    }

    static final Boolean resolveMandatoryValue (OperationMap map) {
        return (map == null) ? null : map.get(MANDATORY_FIELD, Boolean.class);
    }

//    static final EndPointAnalysis resolveEndpointValue (Frame frame) {
//        return resolveEndpointValue(getEncodedMap(frame));
//    }
    
    static final EndPointAnalysis resolveEndpointValue (OperationMap map) {
        if (map == null) {
            return null;
        } else {
            return EndPointAnalysis.decode(map.get(ANALYSIS_FIELD, OperationMap.class));
        }
    }
    
//    static final OperationMap getEncodedMap (Frame frame) {
//        return getEncodedMap(frame.getOperation());
//    }
//    
//    static final OperationMap getEncodedMap (Operation op) {
//        return op.get(OPERATION_MAP_NAME, OperationMap.class);
//    }
    /**
     * @param opType The requested {@link OperationType}
     * @return A {@link Collection} of all frames that contain an {@link Operation}
     * of the requested type (starting at the root and descending to the
     * children) (empty if no match)
     */
    public Collection<Frame> getAllFramesOfType(final OperationType opType) {
        return getAllFramesOfType(opType, new LinkedList<Frame>());
    }

    public <C extends Collection<Frame>> C getAllFramesOfType (final OperationType opType, final C framesList) {
        return FrameUtil.getAllFramesOfType(getRootFrame(), opType, framesList);
    }

    private static Frame getFrame(OperationType opType, Frame root) {
        if (root.getOperation().getType().equals(opType)) {
            return root;
        }

        for (Frame child : root.getChildren()) {
            Frame res = getFrame(opType, child);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    private static Frame getFrame(FrameId id, Frame root) {
        if (root.getId().equals(id)) {
            return root;
        }

        for (Frame child : root.getChildren()) {
            Frame res = getFrame(id, child);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    /**
     * Create a new Trace instance.  The Date for the Trace is determined
     * by the start time of the root frame.
     */
    public static Trace newInstance(ServerName server, ApplicationName appName, TraceId id, Frame root) {
        return newInstance(server, appName, id,TraceType.SIMPLE, root,null);
    }

    public static Trace newInstance(ServerName server, ApplicationName appName, TraceId id,TraceType type, Frame root,Map<String,Object>hints) {
        Date date = root.getRange().getStartDate();
        return new Trace(server, appName, date, id, type,root, hints);
    }


    /**
     * Creates a new instance of a trace, specifying the UNKNOWN_SERVER as the server.
     */
    public static Trace newInstance(ApplicationName appName, TraceId id, Frame root) {
        return newInstance(UNKNOWN_SERVER, appName, id, root);
    }

    /**
     * Shallow copy of properties from the original trace replacing the frame stack.
     */
    public static Trace clone(Trace original, Frame root) {
        Trace clone = newInstance(original.getServer(), original.getAppName(), original.getId(),original.getType(), root, original.getHints());
        if (original.isSensitive()) {
            clone.markSensitive();
        }
        
//        if (original.isMandatory()) {
//            clone.markMandatory();
//        }
        
        return clone;
    }

    public static Trace cloneWithNewId(Trace original, Frame root) {
        TraceId id = TraceId.valueOf(/*original.getId() + "-ext"*/);
        Trace clone = newInstance(original.getServer(), original.getAppName(), id,original.getType(), root,original.getHints());
        if (original.isSensitive()) {
            clone.markSensitive();
        }
        
//        if (original.isMandatory()) {
//            clone.markMandatory();
//        }

        return clone;
    }
    
    public static final Comparator<Trace> getComparatorByTime() {
        return new Comparator<Trace>() {
            public int compare(Trace t1, Trace t2) {
                return MathUtil.signOf(t1.getTimestamp() - t2.getTimestamp());
            }
        };
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hashCode(getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Trace other = (Trace) obj;
        return ObjectUtil.typedEquals(getId(), other.getId());
    }

    @Override
    public String toString() {
        String opLabel = "<no root frame>";
        Frame   rootFrame = getRootFrame();
        if (rootFrame != null) {
            opLabel = rootFrame.getOperation().getLabel();
        }
        return "Trace[id=" + getId() + " app=" + getAppName() + "  op="+ opLabel + "]";
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (isSensitive()) {
            // prevent a sensitive object from being serialized, ever
            throw new NotSerializableException("Sensitive trace is not serializable: " + getId());
        }
        out.defaultWriteObject();
    }

    /**
     * Navigate the hierarchy of frames (starting at the root and
     * descending to the children), looking for the
     * last frames (frames with the requested opType,
     * Which don't have children with this opType)
     * 
     * @return empty List if no such frame could be found.
     */
    public List<Frame> getLastFramesOfType(OperationType opType) {
        List<Frame> frames = new ArrayList<Frame>();
        FrameUtil.getLastFramesOfType(opType, getRootFrame(), frames);
        return frames;
    }

    /**
     * Collects all resource keys encoded in external frames
     * @param resourceKeys The {@link Collection} of {@link ResourceKey} to
     * add the extracted resources
     * @return The {@link Collection} of {@link Frame}-s from which data was
     * potentially extracted
     * @see #getExternalFrames()
     * @see #addExternalResourceKeys(Collection, Frame)
     */
    public Collection<Frame> addExternalResourceKeys (Collection<ResourceKey> resourceKeys) {
    	Collection<Frame>	externalFrames=getExternalFrames();
    	if (ListUtil.size(externalFrames) <= 0) {
    		return externalFrames;
    	}

        for (Frame frame : externalFrames) {
        	addExternalResourceKeys(frame, resourceKeys);
        }

		return externalFrames;
    }

    public Collection<Frame> getExternalFrames() {
        return getExternalFrames(getRootFrame(), new LinkedList<Frame>());
    }

    public <C extends Collection<Frame>> C getExternalFrames(final Frame rootFrame, final C framesList) {
        FrameUtil.traverseFrameHierarchy(rootFrame, true, new FrameTraverseCallback() {
                public boolean frameVisited(final Frame frame) {
                	if (isExternalFrame(frame)) {
                		framesList.add(frame);
                	}
                    
                    return true;
                }
            });
        return framesList;
    }

    /**
     * @param frame The {@link Frame} instance
     * @return <code>true</code> if the associated operation contains any
     * non-<code>null</code> value for a key in the {@link #EXTERNAL_KEYS}
     * @see #isExternalFrame(Operation)
     */
    public static final boolean isExternalFrame (Frame frame) {
    	return isExternalFrame(frame.getOperation());
    }

    /**
     * @param op The associated frame {@link Operation}
     * @return <code>true</code> if the operation contains any
     * non-<code>null</code> value for a key in the {@link #EXTERNAL_KEYS}
     */
    public static final boolean isExternalFrame (Operation op) {
        for (String keyName : EXTERNAL_KEYS) {
        	if (op.get(keyName) != null) {
        		return true;
        	}
        }

        return false;
    }
    /**
     * Goes over all the properties defined in {@link #EXTERNAL_KEYS} and checks
     * if <U>any</U> of them has an associated {@link String} value. If so, then
     * it adds it as a {@link ResourceKey}
     * @param frame The {@link Frame} to be checked for potential encoded
     * resource keys
     * @param resourceKeys The {@link Collection} of {@link ResourceKey} to
     * add the extracted resources
     * @return A {@link Map} of the <U>detected</U> resource keys - key=the {@link Operation}
     * property name, value=the associated {@link ResourceKey} value
     */
    public static final Map<String,ResourceKey> addExternalResourceKeys (Frame frame, Collection<ResourceKey> resourceKeys) {
    	return addExternalResourceKeys(frame.getOperation(), resourceKeys);
    }
    
    /**
     * Goes over all the properties defined in {@link #EXTERNAL_KEYS} and checks
     * if <U>any</U> of them has an associated {@link String} value. If so, then
     * it adds it as a {@link ResourceKey}
     * @param op The {@link Operation} to be checked for potential encoded
     * resource keys
     * @param resourceKeys The {@link Collection} of {@link ResourceKey} to
     * add the extracted resources
     * @return A {@link Map} of the <U>detected</U> resource keys - key=the {@link Operation}
     * property name, value=the associated {@link ResourceKey} value
     * @see #extractExternalResourceKeys(Operation)
     */
	public static final Map<String,ResourceKey> addExternalResourceKeys (Operation op, Collection<ResourceKey> resourceKeys) {
		Map<String,ResourceKey>	result=extractExternalResourceKeys(op);
		if (MapUtil.size(result) <= 0) {
			return result;	// debug breakpoint
		}

		resourceKeys.addAll(result.values());
		return result;
    }

	/**
     * Goes over all the properties defined in {@link #EXTERNAL_KEYS} and checks
     * if <U>any</U> of them has an associated {@link String} value. If so, then
     * it extracts it as a {@link ResourceKey}
     * @param op The {@link Operation} to be checked for potential encoded
     * resource keys
     * @return A {@link Map} of the <U>detected</U> resource keys - key=the {@link Operation}
     * property name, value=the associated {@link ResourceKey} value
	 */
	public static final Map<String,ResourceKey> extractExternalResourceKeys (Operation op) {
		Map<String,ResourceKey>	result=null;
    	for (String keyName : EXTERNAL_KEYS) {
    		String	key=op.get(keyName, String.class);
    		if (StringUtil.isEmpty(key)) {
    			continue;
    		}

    		ResourceKey	resKey=ResourceKey.valueOf(key);
    		if (result == null) {
    			result = new TreeMap<String, ResourceKey>();
    		}
    		result.put(keyName, resKey);
    	}

    	if (result == null) {
    		return Collections.emptyMap();
    	} else {
    		return result;
    	}
	}
    // compares members not covered by equals call
    public static boolean compareMembers (Trace t1, Trace t2) {
        if (t1 == t2) {
            return true;
        }
        
        return 
//        		(t1.isMandatory() == t2.isMandatory())
//            && 
            (t1.isSensitive() == t2.isSensitive())
            && (t1.getTimestamp() == t2.getTimestamp())
            && ObjectUtil.typedEquals(t1.getAppName(), t2.getAppName())
            && ObjectUtil.typedEquals(t1.getServer(), t2.getServer())
//            && ObjectUtil.typedEquals(t1.getEndpoint(), t2.getEndpoint())
             ;
    }
    public InsightJsonObject getExtraInfo() throws JSONException
    {
    	InsightJsonObject jsonObj = new InsightJsonObject();
    	String requestKey = FrameBuilder.HINT_HTTP_REQUEST;
    	String respKey = FrameBuilder.HINT_HTTP_RESPONSE;
    	String hasExceptionKey = FrameBuilder.HINT_HAS_EXCEPTION;
    	boolean isStatic = false;
    	int hasException = 0;
    	if(null != hints.get(hasExceptionKey))
    		hasException = (Integer) hints.get(hasExceptionKey);
    	
    	jsonObj.put(hasExceptionKey,hasException);
    	if(null != hints.get(FrameBuilder.HINT_STATIC_PATH))
    	{
    		isStatic= (Boolean) hints.get(FrameBuilder.HINT_STATIC_PATH);
    	}
    	jsonObj.put("is_static_path", isStatic?1:0);
    	if(null!= type && type.equals(TraceType.HTTP))
    	{
    		if(null != hints.get(requestKey))
    		{
    			OperationMap reqMap = (OperationMap) hints.get(requestKey);	
    			jsonObj.put("http_request",reqMap.toJson());
    		}
    		OperationMap resp = null;
    		if(null != hints.get(respKey))
    		{
    			resp = (OperationMap) hints.get(respKey);
    			if(null != hints.get(FrameBuilder.HINT_HTTP_RESPONSE_BODY))
    			{
    				@SuppressWarnings("unchecked")
					List<Byte> bodyBytes = (List<Byte>) hints.get(FrameBuilder.HINT_HTTP_RESPONSE_BODY);
    				byte [] bytes = new byte[bodyBytes.size()];

//    				StringBuilder body = new StringBuilder();
//    				for(String bodyStr:bodys)
//    					body.append(bodyStr);
    				int i = 0;
    				for(Byte bodyByte:bodyBytes)
    				{
    					bytes[i] = bodyByte;i++;
    				}
    				byte[] encode = Base64.encodeBase64(bytes);
    				resp.putAny("body",new String(encode));
    			}
    		}
    		jsonObj.put("http_response",resp==null?"":resp.toJson());
    		if(null != hints.get("exception"))
        		jsonObj.put("exception",hints.get("exception"));

    	}
    	return jsonObj;
    }
}
