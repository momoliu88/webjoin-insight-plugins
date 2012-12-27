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

package com.ebupt.webjoin.insight.plugin.redis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.topology.*;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.util.ListUtil;



/**
 * 
 */
public class RedisExternalResourceAnalyzer extends AbstractExternalResourceAnalyzer {
	public static final OperationType TYPE =  OperationType.valueOf("redis-client-method");

	public RedisExternalResourceAnalyzer () {
		super(TYPE);
	}

	public List<ExternalResourceDescriptor> locateExternalResourceName(Trace trace, Collection<Frame> dbFrames) {
		if (ListUtil.size(dbFrames) <= 0) {
		    return Collections.emptyList();
		}

		List<ExternalResourceDescriptor> dbDescriptors = new ArrayList<ExternalResourceDescriptor>(dbFrames.size());
		for (Frame dbFrame : dbFrames) {
			Operation op = dbFrame.getOperation();
			String host = op.get("host", String.class);           
			Integer portProperty = op.get("port", Integer.class);
			int port = portProperty == null ? -1 : portProperty.intValue();
			String color = colorManager.getColor(op);
			String dbName = op.get("dbName", String.class);
			
			String redisHash = MD5NameGenerator.getName(dbName+host+port);
			
			dbDescriptors.add(new ExternalResourceDescriptor(dbFrame,
					"redis:" + redisHash,
					dbName,
					ExternalResourceType.DATABASE.name(),
					"Redis",
					host,
					port,
                    color, false) );			
		}
		
		return dbDescriptors;
	}

}