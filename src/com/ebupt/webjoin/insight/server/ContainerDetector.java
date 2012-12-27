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
package com.ebupt.webjoin.insight.server;

import com.ebupt.webjoin.insight.util.ClassUtil;
import com.ebupt.webjoin.insight.util.StringUtil;


public final class ContainerDetector {
    private static ContainerType _type;
    
    public static final ContainerInformationProvider getContainerInformationProvider () {
    	ContainerType	ct=getContainerType();
    	return ct.getInformationProvider();
    }

    public static final ContainerType getContainerType() {
        if (_type == null) {
            _type = resolveContainerType();
        }
        
        return _type;
    }

    private ContainerDetector () {
    	throw new UnsupportedOperationException("No instance");
    }

    private static ContainerType resolveContainerType() {
    	ClassLoader	cl=ClassUtil.getDefaultClassLoader(ContainerDetector.class);
        for(ContainerType ct : ContainerType.values()) {
            if (ct.isUnknown()) {
            	continue;
            }

            if (validateByDetectionClass(ct, cl)) {
                return ct;
            }
        }
        
        return ContainerType.UNKNOWN;
    }

    private static boolean validateByDetectionClass (ContainerType ct, ClassLoader cl) {
        String detectionClass = ct.getContainerDetectionClass();
        if (StringUtil.isEmpty(detectionClass)) {
        	return false;
        }

        if (ClassUtil.isPresent(detectionClass, cl)) {
        	return true;
        }

        return false;
    }
    /**
     * For unit tests
     */
    static void clearType() {
        _type = null;
    }
}
