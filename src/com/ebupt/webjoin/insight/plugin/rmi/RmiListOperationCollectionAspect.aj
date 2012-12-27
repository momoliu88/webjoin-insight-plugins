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

package com.ebupt.webjoin.insight.plugin.rmi;

import java.rmi.registry.Registry;

import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.ebupt.webjoin.insight.collection.OperationCollectionAspectSupport;
import com.ebupt.webjoin.insight.collection.OperationCollector;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationList;
import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.ExceptionUtils;
 

/**
 */
public aspect RmiListOperationCollectionAspect extends OperationCollectionAspectSupport {

	public RmiListOperationCollectionAspect() {
		super();
	}

	public pointcut list() : execution (* Registry.list());
	@SuppressAjWarnings("adviceDidNotMatch")
	String[] around()
 	   : list() && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart)) {
		Operation op = new Operation()
							.type(RmiDefinitions.RMI_LIST)
							.label("List bound remotes");
		OperationCollector collector = getCollector();
		collector.enter(op);
		try {
			String[] res = proceed();
		
			OperationList names = op.createList(RmiDefinitions.LIST_ATTR);
			if (ArrayUtil.length(res) > 0) {
				for (String name : res) {
					names.add(name);
				}
			}
		
			collector.exitNormal(res);
			return res;
		} catch (Exception e) {
			collector.exitAbnormal(e);
			ExceptionUtils.rethrowException(e);
			return null;
		}
		
		
	}

	@Override
	public String getPluginName() {
		return RmiPluginRuntimeDescriptor.PLUGIN_NAME;
	}

}
