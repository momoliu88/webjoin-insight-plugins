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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StreamCorruptedException;
import java.util.Comparator;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.operation.OperationMap;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.FrameUtil;
import com.ebupt.webjoin.insight.util.MathUtil;
import com.ebupt.webjoin.insight.util.ObjectUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

 

/**
 * This object represents information about a single Trace, concerning which
 * resource was identified as the endpoint and how long the response took.
 * 
 * A {@link Trace} is analyzed to determine what information best describes the
 * end point into the hosted application. (did it come from an HTTP request? Or
 * was it more fine grained, like a Spring MVC request)
 */
public class EndPointAnalysis implements Externalizable {
    private static final long serialVersionUID = 791131290397636138L;
    /**
     * Default name used to encode the contents into an {@link Operation}
     * using an {@link OperationMap}
     */
    public static final String  OPERATION_MAP_NAME=EndPointAnalysis.class.getSimpleName();
    public static final String  LABEL_FIELD="resourceLabel",
                                EXAMPLE_FIELD="example",
                                ENDPOINT_FIELD="endPoint",
                                SCORE_FIELD="score",
                                // special field used to indicate to ignore encoding even if found
                                IGNORED_FIELD="ignored";
    /**
     * Minimum allowed score value - <B>Caveat:</B> should <U>not</U> be used
     * as a real score but rather to signal that the score is irrelevant (e.g.,
     * the frame to be scored is not one expected by the analyzer)
     */
    public static final int	MIN_SCORE_VALUE=Integer.MIN_VALUE;
    /**
     * Scoring value returned by analyzers that implement a &quot;catch-all&quot;
     * functionality - i.e., one that does not look for a specific frame, but rather
     * serve as default endpoints 
     */
    public static final int	DEFAULT_LAYER_SCORE=MIN_SCORE_VALUE + 1;
    /**
     * Score used by analyzers that look for a specific frame, but are allowed to
     * &quot;trump&quot; only {@link #DEFAULT_LAYER_SCORE} ones
     */
    public static final int	TOP_LAYER_SCORE=DEFAULT_LAYER_SCORE + 1;
    /**
     * Score used by analyzers that look for a specific frame, but are allowed to
     * &quot;trump&quot; only {@link #TOP_LAYER_SCORE} ones
     */
    public static final int	CEILING_LAYER_SCORE=TOP_LAYER_SCORE + 1;
    /**
     * Placeholder used by {@link #getHttpExampleRequest(Frame)} if no method available
     */
    public static final String	UNKNOWN_HTTP_METHOD="???";
    /**
     * Placeholder used by {@link #getHttpExampleRequest(Frame)} if no URI available
     */
    public static final String	UNKNOWN_HTTP_URI="<UNKNOWN>";
    
    private String resourceLabel;
    private String example;
    private EndPointName  endPoint;
    private int score;
    private transient Operation source;

    public EndPointAnalysis () {    // special constructor required for serialization (must be public for various reasons)
        super();
    }

    public EndPointAnalysis(EndPointName endPointName, String label, String exampleText, int scoreValue) {
        this(endPointName, label, exampleText, scoreValue, null);
    }

    public EndPointAnalysis(EndPointName endPointName, String label, String exampleText, int scoreValue, Operation sourceOperation) {
        this.endPoint = endPointName;
        this.resourceLabel = label;
        this.example = exampleText;
        this.score = scoreValue;
        this.source = sourceOperation;
    }
    
    public EndPointName getEndPointName() {
        return endPoint;
    }

    /**
     * Returns the label of the {@link Resource} that this EndPoint should be
     * associated with.
     */
    public String getResourceLabel() {
        return resourceLabel;
    }

    /**
     * Return a String representing an example request. This can be used by a UI
     * to show how a specific endpoint was accessed.
     * 
     * ex: GET /foobar?arg=blah
     */
    public String getExample() {
        return example;
    }

    /**
     * Return an integer about 'how sure' we are that this EndPoint is the best
     * description for the trace. A score of 0 means not very sure at all,
     * and a score of 100 means more sure than 70.
     * 
     * This is a simple way to allow us to choose a single {@link EndPointAnalysis}
     * from a list of {@link EndPointAnalysis}
     */
    public int getScore() {
        return score;
    }
    
    /**
     * @return The {@link Operation} from which this endpoint was created - may
     * be <code>null</code>
     */
    public Operation getSourceOperation() {
        return source;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(serialVersionUID);
        out.writeObject(resourceLabel);
        out.writeObject(example);
        out.writeObject(endPoint);
        out.writeInt(score);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        long version = in.readLong();
        if (version != serialVersionUID) {
            throw new StreamCorruptedException("Mismatched serialVersionUID: expected=" + serialVersionUID + ", actual=" + version);
        }
        
        resourceLabel = (String) in.readObject();
        example = (String) in.readObject();
        endPoint = (EndPointName) in.readObject();
        score = in.readInt();
    }
    
    /**
     * Encodes the current contents into an {@link OperationMap} 
     * @param op The {@link Operation} into which to create the encoded map
     * @return The {@link OperationMap} instance containing the encoded information
     * @see #OPERATION_MAP_NAME
     * @see #encode(OperationMap)
     */
    public OperationMap encode (Operation op) {
        return encode(op.createMap(OPERATION_MAP_NAME));
    }
    
    /**
     * Encodes the current contents into an {@link OperationMap} 
     * @param map The {@link OperationMap} into which to encode the contents
     * @return Same as input instance
     */
    public OperationMap encode (OperationMap map) {
        EndPointName    name=getEndPointName();
        return map.putAnyNonEmpty(ENDPOINT_FIELD, (name == null) ? null : name.getName())
                  .putAnyNonEmpty(LABEL_FIELD, getResourceLabel())
                  .putAnyNonEmpty(EXAMPLE_FIELD, getExample())
                  .put(SCORE_FIELD, getScore())
                  ;
    }
    
    @Override
    public int hashCode() {
        return ObjectUtil.hashCode(getEndPointName())
             + getScore()
             ;
    }

    // NOTE: checks only name and score
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;

        EndPointAnalysis    other=(EndPointAnalysis) obj;
        return ObjectUtil.typedEquals(getEndPointName(), other.getEndPointName())
            && (getScore() == other.getScore())
            ;
    }

    @Override
    public String toString() {
        return getEndPointName() + "[" + getResourceLabel() + "]@" + getScore() + ": " + getExample();
    }

    /**
     * Compares 2 {@link EndPointAnalysis} scores - <B>NOTE:</B> the <U>highest</U> score comes 1st
     */
    public static final Comparator<EndPointAnalysis> BY_SCORE_COMPARATOR=new Comparator<EndPointAnalysis>() {
			public int compare(EndPointAnalysis o1, EndPointAnalysis o2) {
				int	s1=o1.getScore(), s2=o2.getScore();
				return MathUtil.signOf(s2 - s1);
			}
    	};

    // compares members not covered by equals call
    public static final boolean compareMembers (EndPointAnalysis a1, EndPointAnalysis a2) {
        if (a1 == a2) {
            return true;
        }

        return ObjectUtil.typedEquals(a1.getResourceLabel(), a2.getResourceLabel())
            && ObjectUtil.typedEquals(a1.getExample(), a2.getExample())
             ;
    }
    /**
     * @param op The {@link Operation} from which to decode the data
     * @return The decoded data or <code>null</code> if no encoded data found
     * @throws IllegalArgumentException If incomplete encoding
     * @see #OPERATION_MAP_NAME
     * @see #decode(OperationMap)
     */
    public static final EndPointAnalysis decode (Operation op) throws IllegalArgumentException {
        return decode(getEncodedMap(op));
    }

    public static final OperationMap getEncodedMap (Operation op) {
        return op.get(OPERATION_MAP_NAME, OperationMap.class);
    }

    public static final OperationMap setIgnored (OperationMap map, boolean ignored) {
        return map.put(IGNORED_FIELD, ignored);
    }
    /**
     * @param map The {@link OperationMap} from which to decode the data
     * @return The decoded data or <code>null</code> if no encoded data found
     * @throws IllegalArgumentException If incomplete encoding
     */
    public static final EndPointAnalysis decode (OperationMap map) throws IllegalArgumentException {
        if (map == null) {
            return null;
        }

        Boolean ignored=map.get(IGNORED_FIELD, Boolean.class);
        if ((ignored != null) && ignored.booleanValue()) {
            return null;
        }

        String  name=map.get(ENDPOINT_FIELD, String.class);
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException("Missing end point name");
        }

        Number  score=map.get(SCORE_FIELD, Number.class);
        if (score == null) {
            throw new IllegalArgumentException("No score found");
        }

        EndPointAnalysis    analysis=new EndPointAnalysis();
        analysis.endPoint = EndPointName.valueOf(name);
        analysis.resourceLabel = map.get(LABEL_FIELD, String.class);
        analysis.example = map.get(EXAMPLE_FIELD, String.class);
        analysis.score = score.intValue();
        return analysis;
    }
    
    /**
     * Converts a frame's depth value into a score value by converting it into
     * the depth's <U>negative</U> value (except for zero - i.e., root)
     * @param depth Current depth
     * @return Assigned score value
     * @throws IllegalArgumentException if negative depth value provided
     */
    public static final int depth2score (int depth) throws IllegalArgumentException {
    	if (depth < 0) {
    		throw new IllegalArgumentException("depth2score(" + depth + ") negative depth N/A");
    	}

    	return 0 - depth;
    }
    
    /**
     * Attempts to locate the 1st ancestor that is of {@link OperationType#HTTP}
     * and build an informative label from it. If not found, then uses the
     * original frame's label. <B>Note:</B> assumes that the required data for
     * the label has been encoded in the associated {@link Operation} in a
     * certain manner (see HTTP frame generating plugins...)
     * @param frame The {@link Frame} for which to generate the HTTP label
     * @return The generated label
     * @see #createHttpExampleRequest(Frame)
     */
    public static final String getHttpExampleRequest(Frame frame) {
        Frame httpFrame=FrameUtil.getFirstParentOfType(frame, OperationType.HTTP);
        if (httpFrame != null) {
            return createHttpExampleRequest(httpFrame);
        }

    	Operation	op=frame.getOperation();
    	return op.getLabel();
    }

    /**
     * Creates an HTTP informative label from the given frame. <B>Note:</B>
     * assumes that the required data for the label has been encoded in the
     * associated {@link Operation} in a certain manner (see HTTP frame
     * generating plugins...)
     * @param httpFrame The {@link Frame} to be used to generate the label
     * (ignored if <code>null</code>). <B>Note:</B> does not check the
     * {@link Operation#getType()} to see if it is a {@link OperationType#HTTP}
     * @return The generated label (or <code>null</code> if no frame).
     * @see #createHttpExampleRequest(Operation)
     */
    public static final String createHttpExampleRequest(Frame httpFrame) {
    	return createHttpExampleRequest((httpFrame == null) ? null : httpFrame.getOperation());
    }

    /**
     * Creates an HTTP informative label from the given operation. <B>Note:</B>
     * assumes that the required data for the label has been encoded in the
     * associated {@link Operation} in a certain manner (see HTTP frame
     * generating plugins...)
     * @param op The {@link Operation} to be used for creating the label.
     * ignored if <code>null</code>). <B>Note:</B> does not check the
     * {@link Operation#getType()} to see if it is a {@link OperationType#HTTP}
     * @return The generated label (or <code>null</code> if no operation or details).
     * @see #createHttpExampleRequest(OperationMap)
     */
    public static final String createHttpExampleRequest(Operation op) {
    	return createHttpExampleRequest((op == null) ? null : op.get("request", OperationMap.class));
    }

    /**
     * Creates an HTTP informative label from the given request details. <B>Note:</B>
     * assumes that the required data for the label has been encoded in the
     * associated {@link OperationMap} in a certain manner (see HTTP frame
     * generating plugins...)
     * @param requestDetails The {@link OperationMap} to be used for creating the
     * label (ignored if <code>null</code>). <B>Note:</B> if the request details
     * have not been encoded in the expected manner, some default(s) are used
     * @return The generated label (or <code>null</code> if no request details).
     */
    public static final String createHttpExampleRequest(OperationMap requestDetails) {
    	if (requestDetails == null) {
    		return null;
    	}

        String			method=resolveStringValue(requestDetails, "method", UNKNOWN_HTTP_METHOD);
        String			uri=resolveStringValue(requestDetails, OperationFields.URI, UNKNOWN_HTTP_URI);
        String			queryString=resolveStringValue(requestDetails, "queryString", "");
        StringBuilder	result=new StringBuilder(StringUtil.getSafeLength(method)
        									   + StringUtil.getSafeLength(uri)
        									   + StringUtil.getSafeLength(queryString)
        									   + Byte.SIZE	/* a little extra */)
        							.append(method)
        							.append(' ')
        							.append(uri)
        							;
        if (!StringUtil.isEmpty(queryString)) {
        	result.append('?').append(queryString);
        }

        return result.toString();
    }

    private static String resolveStringValue (OperationMap details, String key, String defaultValue) {
    	if (details == null) {
    		return defaultValue;
    	} else {
    		return details.get(key, String.class, defaultValue);
    	}
    }
}
