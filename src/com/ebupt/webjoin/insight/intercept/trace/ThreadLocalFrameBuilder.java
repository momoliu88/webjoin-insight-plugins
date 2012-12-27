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

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.intercept.DelegatingFrameBuilderCallbacks;
import com.ebupt.webjoin.insight.intercept.endpoint.EndPointPopulator;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.util.time.NanoStopWatch;

 
/**
 * Keeps a {@link SimpleFrameBuilder} stored in ThreadLocal storage.
 */
public class ThreadLocalFrameBuilder implements FrameBuilder {
    private static final Logger logger = Logger.getLogger(ThreadLocalFrameBuilder.class.getName());
    private final ThreadLocal<SimpleFrameBuilder> _builders = new ThreadLocal<SimpleFrameBuilder>();
    private final FrameBuilderCallback callbacks;

    public ThreadLocalFrameBuilder() {
        this.callbacks = new DelegatingFrameBuilderCallbacks();
    }
    
    public ThreadLocalFrameBuilder(FrameBuilderCallback callback) {
        this.callbacks = callback;
    }
    
    public void enter(Operation operation) {
        SimpleFrameBuilder myBuilder = getMyThreadBuilder();
        if (myBuilder == null) {
            myBuilder = createMyBuilder();
        }

        myBuilder.enter(operation);
    }

    private SimpleFrameBuilder createMyBuilder() {
        SimpleFrameBuilder myBuilder;
        myBuilder = new SimpleFrameBuilder(new NanoStopWatch.NanoStopWatchFactory(), callbacks);
        setMyThreadBuilder(myBuilder);
        return myBuilder;
    }
    
    public Frame peekFrame() {
        SimpleFrameBuilder myBuilder = getMyThreadBuilder();
        if (myBuilder == null) {
            return null;
        }
        return myBuilder.peekFrame();
	}

	public Operation peek() {
        SimpleFrameBuilder myBuilder = getMyThreadBuilder();
        if (myBuilder == null) {
            return null;
        }
        return myBuilder.peek();
    }
    
    public void discard(Frame frame) {
        SimpleFrameBuilder myBuilder = getMyThreadBuilder();
        if (myBuilder != null) {
            myBuilder.discard(frame);
        }
    }

    public Frame exit() {
        SimpleFrameBuilder myBuilder = getMyThreadBuilder();
        if (myBuilder == null) {
            logger.log(Level.SEVERE, "Imbalanced frame stack!  (exit() called too many times)");
            return null;
        }

        Frame res = myBuilder.exit();
        // The frame could have been dumped if the context was
        // ignored
        if (res == null || res.isRoot()) {
            clearMyThreadBuilder();
        }
        return res;
    }
    
    public void dump() {
        clearMyThreadBuilder();
    }

    public SimpleFrameBuilder getMyThreadBuilder() {
        return _builders.get();
    }

    public void setMyThreadBuilder(SimpleFrameBuilder builder) {
        _builders.set(builder);
    }

    public SimpleFrameBuilder clearMyThreadBuilder() {
    	SimpleFrameBuilder prev = getMyThreadBuilder();
        _builders.set(null);
        return prev;
    }

    public <T> T getHint(String hint, Class<T> type) {
		Object	value=getHint(hint);
		if (value == null) {
			return null;
		}

		if (type.isAssignableFrom(value.getClass())) {
			return type.cast(value);
		} else {
			return null;
		}
	}

    public Object getHint(String hint) {
        FrameBuilder myBuilder = getMyThreadBuilder();
        if (myBuilder == null) {
            return null;
        }
        return myBuilder.getHint(hint);
    }

    public void setHint(String hint, Object value) {
        FrameBuilder myBuilder = getMyThreadBuilder();
        if (myBuilder == null) {
            return;
        }
        myBuilder.setHint(hint, value);
    }

    public void setHintIfRoot(String hint, Object value) {
        FrameBuilder myBuilder = getMyThreadBuilder();
        if (myBuilder == null) {
            if (hint.equals(EndPointPopulator.HINT_NAME)) {
                myBuilder = createMyBuilder();
            } else {
                return;
            }
        }
        myBuilder.setHintIfRoot(hint, value);
    }

    public boolean isFrameInTrace(Frame frame) {
        SimpleFrameBuilder myBuilder = getMyThreadBuilder();
        if (myBuilder != null) {
            return myBuilder.isFrameInTrace(frame);
        }
        
        return false;
    }

	public boolean isCurrentRootFrame(Frame frame) {
        SimpleFrameBuilder myBuilder=getMyThreadBuilder();
        if (myBuilder != null) {
            return myBuilder.isCurrentRootFrame(frame);
        }
        
        return false;
	}

	@Override
	public int getdepth() {
		if(this._builders.get() == null)return 0;
		return this._builders.get().getdepth();
	}

	@Override
	public Map<String, Object> getHints() {
        SimpleFrameBuilder myBuilder = getMyThreadBuilder();
		if(myBuilder != null)
			return myBuilder.getHints();
		return null;
	}
}
