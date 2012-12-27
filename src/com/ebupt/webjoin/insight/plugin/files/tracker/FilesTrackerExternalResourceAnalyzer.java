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

package com.ebupt.webjoin.insight.plugin.files.tracker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.topology.AbstractExternalResourceAnalyzer;
import com.ebupt.webjoin.insight.intercept.topology.ExternalResourceDescriptor;
import com.ebupt.webjoin.insight.intercept.topology.ExternalResourceType;
import com.ebupt.webjoin.insight.intercept.topology.MD5NameGenerator;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.StringUtil;


public class FilesTrackerExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
    public FilesTrackerExternalResourceAnalyzer () {
        super(FilesTrackerDefinitions.TYPE);
    }

	public Collection<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> frames) {
		if (ListUtil.size(frames) <= 0) {
		    return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> descriptors = new ArrayList<ExternalResourceDescriptor>(frames.size());
		for (Frame frame : frames) {			
            Operation    op=frame.getOperation();
			String       path=op.get(FilesTrackerDefinitions.PATH_ATTR, String.class);
			if (StringUtil.isEmpty(path)) {
				continue;
			}

			String hashString = MD5NameGenerator.getName(path);
			String color = colorManager.getColor(op);
			ExternalResourceDescriptor desc =
			        new ExternalResourceDescriptor(frame,
			                                       FilesTrackerDefinitions.TYPE.getName() + ":" + hashString,
			                                       path,
			                                       ExternalResourceType.FILESTORE.name(),
			                                       FilesTrackerDefinitions.TYPE.getName(),
			                                       color, false);
			descriptors.add(desc);			
		}
		
		return descriptors;
	}
}
