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
package com.ebupt.webjoin.insight.plugin.socket;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.collection.method.MethodOperationCollectionAspect;
import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;

 

/**
 * 
 */
public abstract aspect SocketOperationCollectionAspectSupport
                extends MethodOperationCollectionAspect {
    private static final InterceptConfiguration configuration = InterceptConfiguration.getInstance();
    private final SocketCollectOperationContext collectContext=new SocketCollectOperationContext();
    protected final Logger  logger=Logger.getLogger(getClass().getName());
    protected SocketOperationCollectionAspectSupport() {
        super();
    }

    SocketCollectOperationContext getSocketCollectOperationContext () {
        return collectContext;
    }

    protected Operation createOperation (Operation op, String action, String addr, int port) {
        if (collectContext.updateObscuredAddressValue(addr)) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("createOperation(" + action + ") obscured: " + addr);
            }
        }

        return SocketDefinitions.initializeOperation(op, action, addr, port);
    }

    protected boolean collectExtraInformation ()
    {
        return FrameBuilder.OperationCollectionLevel.HIGH.equals(configuration.getCollectionLevel());
    }

    @Override
    public String getPluginName() {
        return SocketPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
