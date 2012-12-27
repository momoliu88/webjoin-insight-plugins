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

package com.ebupt.webjoin.insight.plugin.mongodb;

import java.util.List;

import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.collection.AbstractOperationCollectionAspect;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationList;
import com.mongodb.CommandResult;
import com.mongodb.DB;

public aspect MongoDbOperationCollectionAspect extends AbstractOperationCollectionAspect {
	public MongoDbOperationCollectionAspect () {
		super();
	}

    public pointcut collectionPoint(): execution(CommandResult DB.command(..));

    @Override
    protected Operation createOperation(final JoinPoint jp) {
        Operation op = new Operation()
        					.label("MongoDB: DB." + jp.getSignature().getName() + "()")
        					.type(MongoDBOperationExternalResourceAnalyzer.TYPE);
        OperationList opList = op.createList("args");

        List<String> args = MongoArgumentUtils.toString(jp.getArgs());
        for (String arg : args) {
            opList.add(arg);
        }
        
        try {
        	MongoArgumentUtils.putDatabaseDetails(op, (DB) jp.getTarget());
		} catch (Exception e) {
			// ignored
		}
        
        return op;
    }

    @Override
    public String getPluginName() {
        return MongoDBPluginRuntimeDescriptor.PLUGIN_NAME;
    }
}
