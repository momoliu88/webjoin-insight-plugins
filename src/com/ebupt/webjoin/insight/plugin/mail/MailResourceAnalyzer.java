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
package com.ebupt.webjoin.insight.plugin.mail;

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



public class MailResourceAnalyzer extends AbstractExternalResourceAnalyzer {
	public static final String RESOURCE_TYPE=ExternalResourceType.EMAIL.name();
	
	public MailResourceAnalyzer () {
		super(MailDefinitions.SEND_OPERATION);
	}

	public List<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> mailFrames) {
		if (ListUtil.size(mailFrames) <= 0) {
            return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> mailDescriptors = new ArrayList<ExternalResourceDescriptor>(mailFrames.size());
		for (Frame mailFrame : mailFrames) {
			Operation op = mailFrame.getOperation();

			String host = op.get(MailDefinitions.SEND_HOST, String.class);            
			Number portProperty = op.get(MailDefinitions.SEND_PORT, Number.class);
			int port = portProperty == null ? -1 : portProperty.intValue();
			String protocol = op.get(MailDefinitions.SEND_PROTOCOL, String.class); 
			String label = protocol.toUpperCase() + ":" + host + ((port > 0) ? (":" + port) : "");
			String hashString = MD5NameGenerator.getName(label);
            String color = colorManager.getColor(op);

			ExternalResourceDescriptor descriptor = new ExternalResourceDescriptor(
					mailFrame, protocol + ":" + hashString, label, RESOURCE_TYPE, protocol, host, port, color, false);
			mailDescriptors.add(descriptor);            
		}

		return mailDescriptors;
	}

}
