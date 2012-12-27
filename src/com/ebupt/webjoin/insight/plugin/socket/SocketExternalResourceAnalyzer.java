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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.ebupt.webjoin.insight.intercept.operation.*;
import com.ebupt.webjoin.insight.intercept.topology.*;
import com.ebupt.webjoin.insight.intercept.trace.*;
import com.ebupt.webjoin.insight.util.*;
 

/**
 * Extracts {@link SocketDefinitions#CONNECT_ACTION} target addresses as
 *  {@link ExternalResourceType#SERVER} {@link ExternalResourceDescriptor}-s
 */
public class SocketExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
	
    public SocketExternalResourceAnalyzer() {
       super(SocketDefinitions.TYPE);
    }

    public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame>   framesList) {
        if (ListUtil.size(framesList) <= 0) {
            return Collections.emptyList();
        }

        Set<ExternalResourceDescriptor> resSet=new HashSet<ExternalResourceDescriptor>(framesList.size());
        for (Frame frame : framesList) {
            ExternalResourceDescriptor  res=extractExternalResourceDescriptor(frame);
            if (res == null) {  // can happen if failed to parse the URI somehow
                continue;
            }
            
            if (!resSet.add(res))
                continue;   // debug breakpoint
        }
        
        return resSet;
    }

    ExternalResourceDescriptor extractExternalResourceDescriptor (Frame frame) {
        Operation   op=(frame == null) ? null : frame.getOperation();
        String      action=(op == null) ? null : op.get(SocketDefinitions.ACTION_ATTR, String.class);
        if (!SocketDefinitions.CONNECT_ACTION.equals(action)) {
            return null;
        }

        String  addr=op.get(SocketDefinitions.ADDRESS_ATTR, String.class);
        int     port=op.get(SocketDefinitions.PORT_ATTR, Number.class).intValue();
        String  uri=op.get(OperationFields.URI,String.class);
        ExternalResourceType    type=(uri == null) ? ExternalResourceType.SERVER : ExternalResourceType.WEB_SERVER;
        String color = colorManager.getColor(op);
        
        return new ExternalResourceDescriptor(frame,
        									  MD5NameGenerator.getName(addr + ":" + port),
                                              op.getLabel(),
                                              type.name(),
                                              null,
                                              addr,
                                              port,
                                              color, false);
    }
}
