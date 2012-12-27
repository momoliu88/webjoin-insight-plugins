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

package com.ebupt.webjoin.insight.plugin.portlet;

import javax.portlet.Event;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletRequest;
import javax.portlet.ProcessEvent;

import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.intercept.operation.Operation;


/**
 * This aspect create insight operation for Portlet event
 * @type: portlet-event
 */
public privileged aspect EventOperationCollectionAspect extends GenericOperationCollectionAspect {
    public EventOperationCollectionAspect () {
    	super();
    }

    public pointcut collectionPoint() : execution(void javax.portlet.EventPortlet+.processEvent(EventRequest, EventResponse)) ||
    									execution(@ProcessEvent void *(EventRequest, EventResponse));

	@Override
	protected Operation createOperation(JoinPoint jp) {
		Object[] 	 args=jp.getArgs();
		EventRequest req=(EventRequest)args[0];
		Event 		 event=req.getEvent();
		
		return createOperation(jp, OperationCollectionTypes.EVENT_TYPE)
				.putAnyNonEmpty("eventName", event.getName())
				.putAny("eventValue", event.getValue())
				.putAnyNonEmpty("eventPhase", req.getParameter(PortletRequest.EVENT_PHASE))
				;
	}
}
