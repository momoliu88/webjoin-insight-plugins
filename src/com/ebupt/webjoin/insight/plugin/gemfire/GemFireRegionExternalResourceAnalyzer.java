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

package com.ebupt.webjoin.insight.plugin.gemfire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.ebupt.webjoin.insight.intercept.operation.*;
import com.ebupt.webjoin.insight.intercept.topology.AbstractExternalResourceAnalyzer;
import com.ebupt.webjoin.insight.intercept.topology.ExternalResourceDescriptor;
import com.ebupt.webjoin.insight.intercept.topology.ExternalResourceType;
import com.ebupt.webjoin.insight.intercept.topology.MD5NameGenerator;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.util.*;
 


public class GemFireRegionExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
	public GemFireRegionExternalResourceAnalyzer () {
		super(GemFireDefenitions.TYPE_REGION.getType());
	}

	public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> frames) {
		if (ListUtil.size(frames) <= 0) {
		    return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> descriptors = new ArrayList<ExternalResourceDescriptor>(frames.size());
		for (Frame frame : frames) {			
            Operation       op = frame.getOperation();
			OperationList   servers = op.get(GemFireDefenitions.FIELD_SERVERS, OperationList.class);
			if ((servers == null) || (servers.size() <= 0)) {
				continue;
			}

			Object regionFullPathObj = op.get(GemFireDefenitions.FIELD_PATH);
			String regionFullPath = StringUtil.safeToString(regionFullPathObj);
            String color = colorManager.getColor(op);
			
			for (int i = 0; i < servers.size(); i++) {
				String server = servers.get(i).toString();
				String name = MD5NameGenerator.getName(server+regionFullPath);
				ExternalResourceDescriptor desc =
						new ExternalResourceDescriptor(frame,
													   GemFireDefenitions.GEMFIRE + ":" + name,
													   regionFullPath,
													   ExternalResourceType.KVSTORE.name(),
													   GemFireDefenitions.GEMFIRE,
													   server,
													   -1,
													   color,
													   false);
				descriptors.add(desc);
			}			
		}
		
		return descriptors;
	}
}
