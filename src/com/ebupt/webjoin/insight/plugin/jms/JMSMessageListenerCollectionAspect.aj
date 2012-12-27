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
package com.ebupt.webjoin.insight.plugin.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.ebupt.webjoin.insight.color.ColorManager;
import com.ebupt.webjoin.insight.intercept.operation.*;
  
public aspect JMSMessageListenerCollectionAspect extends AbstractJMSCollectionAspect {
    public JMSMessageListenerCollectionAspect () {
        super(JMSPluginOperationType.LISTENER_RECEIVE);
    }

    public pointcut messageListener(Message message)
        : execution(void javax.jms.MessageListener+.onMessage(Message))
       && args(message)
       && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
        ;

	@SuppressAjWarnings({"adviceDidNotMatch"})
    before(final Message message) : messageListener(message) {
        JoinPoint jp = thisJoinPoint;
        if (message != null) {
            Operation op = createOperation(jp);
            try {
                applyDestinationData(message, op);
                applyMessageData(message, op);
            } catch (Throwable t) {
                markException("beforeListen", t);
            }

            getCollector().enter(op);

            //Set the color for this frame
            extractColor(new ColorManager.ExtractColorParams() {				
                public String getColor(String key) {
                    try {
                        return message.getStringProperty(key);
                    } catch (JMSException e) {
                        return null;
                    }
                }
            });
        }
    }
    
	@SuppressAjWarnings({"adviceDidNotMatch"})
    after(Message message) : messageListener(message) {
        getCollector().exitNormal();
    }

	@SuppressAjWarnings({"adviceDidNotMatch"})
    after(Message message) throwing(Throwable exception) : messageListener(message) {
        getCollector().exitAbnormal(exception);
    }
}
