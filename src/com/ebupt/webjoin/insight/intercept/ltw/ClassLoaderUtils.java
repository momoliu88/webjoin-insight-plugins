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

package com.ebupt.webjoin.insight.intercept.ltw;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.util.ExceptionUtils;
import com.ebupt.webjoin.insight.util.ExtraReflectionUtils;

 


public abstract class ClassLoaderUtils {
    private static final Method addURLMethod;
    static {
        try {
            if ((addURLMethod=ExtraReflectionUtils.getAccessibleMethod(URLClassLoader.class, "addURL", URL.class)) == null) {
                throw new NoSuchMethodException("Missing accessible 'addURL' method");
            }
        } catch (Exception e) {
            System.err.println("Failed (" + e.getClass().getSimpleName() + ")"
                             + " to get access to URLClassLoader methods: " + e.getMessage());
            throw ExceptionUtils.toRuntimeException(e, true);
        }
    }

    public static InsightClassLoader findInsightWeavingClassLoader(ClassLoader cl) {
        if (cl == null) {
        	return null;
        }
        
        if (cl instanceof InsightClassLoader) {
            return (InsightClassLoader) cl;
        }

        return findInsightWeavingClassLoader(cl.getParent());
    }
    
    public static ApplicationName findApplicationName(ClassLoader cl) {
        InsightClassLoader iwcl = findInsightWeavingClassLoader(cl);
        if (iwcl == null) {
            return ApplicationName.UNKOWN_APPLICATION;
        }
        return iwcl.getApplicationName();
    }

    public static void addUrl (Object loader, URL url) throws Exception {
    	addURLMethod.invoke(loader, url);
    }
}
