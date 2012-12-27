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

package com.ebupt.webjoin.insight.plugin.rabbitmqClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.annotation.SuppressAjWarnings;

import com.ebupt.webjoin.insight.color.ColorManager;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationMap;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
 

public aspect RabbitMQConsumerCollectionAspect extends AbstractRabbitMQCollectionAspect {

    public RabbitMQConsumerCollectionAspect () {
        super(RabbitPluginOperationType.CONSUME);
    }

    // Holds Operations in progress for a given Channel
    // According to the API, the Channel should only be
    // used by one thread at a time. Package private for testing
    static Map<Channel, Operation> opHolder = new ConcurrentHashMap<Channel, Operation>();

    public pointcut handleDelivery(String consumerTag, Envelope envelope, BasicProperties props, byte[] body)
        : execution(void Consumer+.handleDelivery(String, Envelope, BasicProperties, byte[]))
       && args(consumerTag, envelope, props, body)
       && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
        ;
    
    public pointcut basicGet(String queue, boolean ack) 
        : execution(GetResponse Channel+.basicGet(String, boolean)) 
       && args(queue, ack)
       && if(strategies.collect(thisAspectInstance, thisJoinPointStaticPart))
        ;

    @SuppressAjWarnings({"adviceDidNotMatch"})
    before(String queue, boolean ack)
            : basicGet(queue, ack) {
        Channel channel = (Channel) thisJoinPoint.getThis();
        Operation op = createOperation();
        opHolder.put(channel,op);
        getCollector().enter(op);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(String queue, boolean ack) returning(GetResponse resp)
            : basicGet(queue, ack) {
        // get the originating operation
        Channel channel = (Channel) thisJoinPoint.getThis();
        Connection conn = channel.getConnection();
        Operation op = opHolder.remove(channel);
        if (conn != null) {
            applyConnectionData(op, conn);
        }

        BasicProperties	props=resp.getProps();
        if (props != null) {
            applyPropertiesData(op, props);
        }

        Envelope	envelope=resp.getEnvelope();
        if (envelope != null) {
            applyMessageData(op, envelope, resp.getBody());
        }
        getCollector().exitNormal(resp);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(String queue, boolean ack) throwing(Throwable t)
            : basicGet(queue, ack) {
        Channel channel = (Channel) thisJoinPoint.getThis();
        opHolder.remove(channel);

        getCollector().exitAbnormal(t);
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    before(String consumerTag, Envelope envelope, final BasicProperties props, byte[] body)
            : handleDelivery(consumerTag, envelope, props, body) {

        Consumer consumer = (Consumer) thisJoinPoint.getThis();
        Connection conn = null;

        if (consumer instanceof DefaultConsumer) {
            DefaultConsumer dconsumer = (DefaultConsumer) consumer;
            conn = dconsumer.getChannel().getConnection();
        }

        Operation op = createOperation();
        if (conn != null) {
            applyConnectionData(op, conn);
        }
        if (props != null) {
            applyPropertiesData(op, props);
        }
        if (envelope != null) {
            applyMessageData(op, envelope, body);
        }
        getCollector().enter(op);
        
        if (props != null) {
            extractColor(new ColorManager.ExtractColorParams() {
                public String getColor(String key) {
                    String color = null;
                    
                    if (props != null) {
                        Map<String, Object> headers = props.getHeaders();
                        
                        if (headers != null) {
                            Object obj = headers.get(key);
                            color = obj != null ? obj.toString() : null;
                        }
                    }
                    
                    return color;
                }
            });
        }
    }

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(String consumerTag, Envelope envelope, BasicProperties props, byte[] body) returning()
            : handleDelivery(consumerTag, envelope, props, body) {
         getCollector().exitNormal();
    }
    
    @SuppressAjWarnings({"adviceDidNotMatch"})
    after(String consumerTag, Envelope envelope, BasicProperties props, byte[] body) throwing(Throwable t) 
            : handleDelivery(consumerTag, envelope, props, body) {
        getCollector().exitAbnormal(t);
    }

    private Operation applyMessageData(Operation op, Envelope envelope, byte[] body) {
        if (body != null) {
            op.put("bytes", body.length);
        }

        OperationMap map = op.createMap("envelope");
        map.put("deliveryTag", envelope.getDeliveryTag())
           .putAnyNonEmpty("exchange", envelope.getExchange())
           .putAnyNonEmpty("routingKey", envelope.getRoutingKey())
           ;
        return op;
    }

    @Override
	public boolean isEndpoint() {
    	return true;
   	}
}
