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

import static com.ebupt.webjoin.insight.plugin.redis.util.RedisUtil.objectToString;

import java.util.Collection;


import org.aspectj.lang.JoinPoint;
import org.springframework.data.redis.support.collections.AbstractRedisCollection;

import com.ebupt.webjoin.insight.collection.AbstractOperationCollectionAspect;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.StringUtil;


/**
 * Aspect class for AbstractRedisCollection operation.
 */
public aspect RedisCollectionOperationAspect extends AbstractOperationCollectionAspect {

    protected static final OperationType TYPE = OperationType.valueOf("default-redis-collection");

    public pointcut redisCollectionAdd()
        : execution(* AbstractRedisCollection.add*(..));

    public pointcut redisCollectionRemove()
        : execution(* AbstractRedisCollection.remove*(..));

    public pointcut collectionPoint()
        : redisCollectionAdd() || redisCollectionRemove();

    @Override
    protected Operation createOperation(JoinPoint jp) {
        String method = jp.getSignature().getName();
        Operation op = new Operation()
            .type(TYPE)
            .put("method", method);
        AbstractRedisCollection<?> collection = (AbstractRedisCollection<?>)jp.getTarget();
        String collectionKey = collection.getKey();
        if (StringUtil.isEmpty(collectionKey)) {
            collectionKey = "?";
        }
        op.put("collectionKey", collectionKey);
        op.label("RedisCollection: " + collectionKey + "." + method + "()");
        Object[] args = jp.getArgs();
        if(ArrayUtil.length(args) == 1) {
            if(StringUtil.safeCompare(method, "add") == 0) {
                op.put("value", objectToString(args[0]));
            } else if(StringUtil.safeCompare(method, "addAll") == 0) {
                op.put("size", ((Collection<?>)args[0]).size());
            } else if(StringUtil.safeCompare(method, "remove") == 0) {
                op.put("value", objectToString(args[0]));
            } else if(StringUtil.safeCompare(method, "removeAll") == 0) {
                op.put("size", ((Collection<?>)args[0]).size());
            }
        }

        return op;
    }

    @Override
    public String getPluginName() {
        return "redis";
    }
}
