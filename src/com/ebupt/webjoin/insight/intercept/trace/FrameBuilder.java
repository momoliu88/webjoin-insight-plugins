package com.ebupt.webjoin.insight.intercept.trace;

import java.util.Map;

import com.ebupt.webjoin.insight.intercept.operation.Operation;

 
/**
 * A {@link FrameBuilder} is able to create a nested stack of frames by allowing
 * the client to call enter/exit. 
 */
public interface FrameBuilder {
    final static String HINT_APPNAME = "applicationName";
    final static String HINT_TRACETYPE="traceType";
    
    final static String HINT_HTTP_RESPONSE_BODY="httpResponseBody";
    final static String HINT_HTTP_REQUEST="request";
    final static String HINT_HTTP_RESPONSE="response";
    final static String HINT_HAS_EXCEPTION = "has_exception";
    final static String HINT_STATIC_PATH="isStaticPath";
    final static String HINT_ABORTED = "aborted";
    final static String HINT_TRACEID = "traceId";
    final static String HINT_SENSITIVE = "sensitive";
    final static String HINT_DISCARD = "discard";
    final static String HINT_COLLECT_ONLY_ENDPOINTS = "only-endpoints";
    final static String HINT_OPERATION_COLLECT_LEVEL = "op-collect-level";
    final static String HINT_MANDATORY = "mandatory-trace";
    final static String HINT_OBSCURED_REGISTRY = "obscuredRegistry";
    public int getdepth();
    /**
     * Represents a Cross-Cutting notion of "How much data should I collect?"
     * This level can be set on a hint using the HINT_OPERATION_COLLECT_LEVEL
     * key and can be used in various places to adjust the collection overhead.
     */
    public enum OperationCollectionLevel {
        HIGH,
        MEDIUM,
        LOW,
    }

    static final int	DEFAULT_MAX_FRAMES_PER_TRACE=1000;
    /**
     * Arbitrary limit on the number of frames which can be created in a tree
     * A frame stack may never exit if there is a bug in a plugin, or the
     * frame builder itself. This could lead to an endlessly growing frame stack.
     */
    final int MAX_FRAMES_PER_TRACE = 
    		Integer.parseInt(System.getProperty("insight.max.frames", String.valueOf(DEFAULT_MAX_FRAMES_PER_TRACE)));
    
    /**
     * Enter into a new Frame (push it onto the frame stack)
     * 
     * @param operation Operation associated with the frame we are entering.
     */
    void enter(Operation operation);

    /**
     * Exit a frame that has previously been {@link #enter(Operation)}ed.
     * 
     * @return The frame that was exited.
     */
    Frame exit();
    
    /**
     * Discard the given frame
     * 
     * @throws IllegalArgumentException if the given frame is not part of the current trace
     */
    void discard(Frame frame);

    /**
     * @return the current {@link Frame} on top of the stack (<code>null</code> if none) 
     */
    Frame peekFrame ();

    /**
     * @return the current {@link Operation} on top of the stack (<code>null</code> if none) 
     */
    Operation peek();
    
    /**
     * Discard the current frame stack, blocking trace creation
     */
    void dump();
    
    /**
     * Sets a hint within the {@link FrameBuilder}.  A hint is an extra piece
     * of information associated with the frame builder which will subsequently
     * be passed into the {@link FrameBuilderFinishCallback} upon a root
     * frame exiting.
     * 
     * If there is no frame currently being built by the builder, this method
     * should not do anything.
     * 
     * @see #HINT_APPNAME
     */
    void setHint(String hint, Object value);

    /**
     * Sets the given hint, but only if the current working frame is the root frame.
     */
    void setHintIfRoot(String hint, Object value);
    
    /**
     * @param hint Hint name
     * @return null if the hint was not found, else the value for the hint
     * previously set via {@link #setHint(String, Object)}. <B>Note:</B>
     * upon exit of a root frame, this value should return null for all hints.
     */
    Object getHint(String hint);
    
    /**
     * @param hint Hint name
     * @param type Expected type
     * @return The hint value - cast to the type if exists and is compatible,
     * <code>null</code> otherwise
     */
    <T> T getHint (String hint, Class<T> type);
    /**
     * @param frame The {@link Frame} instance
     * @return <code>true</code> if the given frame is part of the current trace
     */
    boolean isFrameInTrace(Frame frame);

    /**
     * @param frame The {@link Frame} instance
     * @return <code>true</code> if the frame is the root
     */
    boolean isCurrentRootFrame (Frame frame);
    Map<String,Object> getHints();
}
