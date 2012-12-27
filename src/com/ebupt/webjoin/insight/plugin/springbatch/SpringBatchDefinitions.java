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

package com.ebupt.webjoin.insight.plugin.springbatch;

import com.ebupt.webjoin.insight.intercept.operation.OperationType;


/**
 * 
 */
public final class SpringBatchDefinitions {
    private SpringBatchDefinitions() {
        throw new UnsupportedOperationException("Construction N/A");
    }

    public static final OperationType   BATCH_TYPE=OperationType.valueOf("spring-batch");
    // some widely used operation attributes
    public static final String  NAME_ATTR="name",
                                JOBNAME_ATTR="jobName",
                                STEPNAME_ATTR="stepName",
                                TYPE_ATTR="batchType",
                                ACTION_ATTR="action",
                                UNKNOWN_VALUE="<unknown>";
}
