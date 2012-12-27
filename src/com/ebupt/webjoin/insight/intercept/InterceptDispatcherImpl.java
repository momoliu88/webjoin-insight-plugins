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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.intercept.trace.Trace;

 
/**
 * Simple dispatcher implementation which keeps references to listeners in a 
 * weak hash map.
 *
 * This dispatcher will also store a buffer of traces if no listeners have been
 * registered.  This allows a dispatcher to register after traces have been
 * generated and still be able to listen for them.
 */
public class InterceptDispatcherImpl implements InterceptDispatcher {
    public static final int MAX_BUFFER_SIZE = 50;   // TODO make it configurable via insight.properties file
    protected final Logger log = Logger.getLogger(getClass().getName());

    private final WeakHashMap<InterceptListener, InterceptListener> listeners = new WeakHashMap<InterceptListener, InterceptListener>();
    private final LinkedList<Trace> traceBuffer = new LinkedList<Trace>();

    public InterceptDispatcherImpl () {
        super();
    }

    public void dispatchTrace(Trace trace) {
        if (!invokeAllListeners(trace)) {
            addTraceToBuffer(trace);
        } else if (log.isLoggable(Level.FINE)) {
            log.fine("Dispatched trace id=" + trace.getId());
        }
    }

    protected boolean invokeAllListeners(Trace trace) {
        boolean dispatched = false;
        /* NOTE !!! this code stops dispatching after the 1st success
         * If this behavior is changed then need to also change CoreInterceptDispatcherImpl
         */
        for (InterceptListener l : listeners.keySet()) {
            dispatched = dispatched || safelyDispatchTrace(l, trace);
        }
        
        return dispatched;
    }

    private boolean safelyDispatchTrace(InterceptListener listener, Trace t) {
        try {
            if (listener instanceof TraceInterceptListener) {
               ((TraceInterceptListener)listener).handleTraceDispatch(t);
                return true;
            }
        } catch(Throwable e) {
            log.log(Level.SEVERE,
                    listener + "#handleTraceDispatch(" + t + ") " + e.getClass().getSimpleName() + ": " + e.getMessage(),
                    e);
        }

        return false;
    }

    private void addTraceToBuffer(Trace trace) {
        int numRemoved=0, currentSize=0;
        synchronized (traceBuffer) {
            traceBuffer.add(trace);
            for (numRemoved = 0; (currentSize=traceBuffer.size()) > MAX_BUFFER_SIZE; numRemoved++) {
                traceBuffer.removeFirst();
            }
        }
        
        if (log.isLoggable(Level.FINE)) {
            log.fine("addTraceToBuffer(" + trace.getId() + ") removed=" + numRemoved + "/current=" + currentSize);
        }
    }

    public void register(InterceptListener listener) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Registering intercept listener: " + listener);
        }
        listeners.put(listener, listener);
        dispatchBuffer();
    }

    /**
     * @return The {@link List} of {@link Trace}-s that have been dispatched
     */
    private List<Trace> dispatchBuffer() {
        List<Trace>  pending=drainTraceBuffer();
        for (InterceptListener l : listeners.keySet()) {
            for (Trace t : pending) {
                safelyDispatchTrace(l, t);
            }
        }

        return pending;
    }

    protected List<Trace> drainTraceBuffer () {
        final List<Trace>   pending;
        synchronized (traceBuffer) {
            if (traceBuffer.isEmpty()) {
                return Collections.emptyList();
            }
            
            pending = new ArrayList<Trace>(traceBuffer);
            traceBuffer.clear();
        }
        
        return pending;
    }

    // Used by tests
    int getBufferSize() {
        synchronized (traceBuffer) {
            return traceBuffer.size();
        }
    }

    public boolean unregister(InterceptListener listener) {
        if (log.isLoggable(Level.FINE)) {
            log.fine("Unregistering listener: " + listener);
        }
        return listeners.remove(listener) != null;
    }

    public List<InterceptListener> getListeners() {
        return new ArrayList<InterceptListener>(listeners.keySet());
    }
}
