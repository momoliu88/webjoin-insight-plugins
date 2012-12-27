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

package com.ebupt.webjoin.insight.plugin.grails;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;

/**
 * This aspect works in combination with the GrailsControllerMetricCollectionAspect.  This 
 * particular aspect only steals off the controller name and saves it in threadlocal.
 */
public aspect GrailsControllerClassAspect {
	public pointcut collectionPoint() : GrailsControllerPointcuts.getControllerClassMethod();

    @SuppressAjWarnings({"adviceDidNotMatch"})
    after() returning(GrailsControllerClass returnValue) : collectionPoint() {
        GrailsControllerStateKeeper.setThreadLocalController(returnValue.getShortName(), 
                                                             returnValue.getFullName());
    }
}
