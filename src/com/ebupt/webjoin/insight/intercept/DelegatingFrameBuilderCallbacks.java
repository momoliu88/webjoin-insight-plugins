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

 

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilderCallback;
import com.ebupt.webjoin.insight.intercept.trace.NullFrameBuilderCallback;

public class DelegatingFrameBuilderCallbacks extends NullFrameBuilderCallback {
    private List<FrameBuilderCallback> rootEnterCallbacks = new CopyOnWriteArrayList<FrameBuilderCallback>();
    private List<FrameBuilderCallback> rootExitCallbacks = new CopyOnWriteArrayList<FrameBuilderCallback>();
    private List<FrameBuilderCallback> childEnterCallbacks = new CopyOnWriteArrayList<FrameBuilderCallback>();
    private List<FrameBuilderCallback> childExitCallbacks = new CopyOnWriteArrayList<FrameBuilderCallback>();

    public DelegatingFrameBuilderCallbacks(FrameBuilderCallback ... callbacks) {
        for (FrameBuilderCallback callback : callbacks) {
            addDelegate(callback);
        }
    }

    @Override
    public final void enterChildFrame(Frame frame) {
        for (FrameBuilderCallback fbc : childEnterCallbacks)
            fbc.enterChildFrame(frame);
    }


    @Override
    public final void exitChildFrame(Frame frame) {
        for (FrameBuilderCallback fbc : childExitCallbacks)
            fbc.exitChildFrame(frame);
    }

    public List<FrameBuilderEvent> listensTo() {
        return null;
    }

    @Override
    public final void enterRootFrame() {
        for (FrameBuilderCallback fbc : rootEnterCallbacks)
            fbc.enterRootFrame();
    }

    @Override
    public final void exitRootFrame(Frame frame, Map<String, Object> hints) {
        for (FrameBuilderCallback fbc : rootExitCallbacks)
            fbc.exitRootFrame(frame, hints);
    }

    public synchronized void addDelegate(FrameBuilderCallback delegate) {
        for (FrameBuilderEvent event : delegate.listensTo()) {
            switch (event) {
               case CHILD_ENTER:
                   childEnterCallbacks.add(delegate);
                   break;
               case CHILD_EXIT:
                   childExitCallbacks.add(delegate);
                   break;
               case ROOT_ENTER:
                   rootEnterCallbacks.add(delegate);
                   break;
               case ROOT_EXIT:
                   rootExitCallbacks.add(delegate);
                   break;
               default:
                   throw new IllegalStateException("Unknown callback event for FrameBuilderCallback");
            }
        }
    }
}
