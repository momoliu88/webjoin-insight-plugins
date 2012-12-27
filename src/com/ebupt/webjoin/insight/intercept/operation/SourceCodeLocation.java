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

package com.ebupt.webjoin.insight.intercept.operation;

import java.io.Serializable;

import com.ebupt.webjoin.insight.util.ObjectUtil;


/**
 * A class which describes a location within source code.
 */
public final class SourceCodeLocation implements Serializable {
    private static final long serialVersionUID = 578263820560526237L;
    private String className;
    private String methodName;
    private int lineNumber;

    /*
     * Constructor and Setter Methods pacakge scope to allow for mutability in deserialization
     */
    SourceCodeLocation() {
        super();
    }

    public SourceCodeLocation(StackTraceElement elem) {
    	this(elem.getClassName(), elem.getMethodName(), elem.getLineNumber());
    }

    public SourceCodeLocation(String typeName, String method, int sourceLineNumber) {
        this.className = typeName;
        this.methodName = method;
        this.lineNumber = sourceLineNumber;
    }

    /**
     * Returns the name of the class associated with the source code locator.  
     * @return a fully-qualified classname, ala Class.getName()
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the simple method name (without arguments)
     */
    public String getMethodName() {
        return methodName;
    }

    public int getLineNumber() {
        return lineNumber;
    }
 
    void setClassName(String name) {
        this.className = name;
    }

    void setMethodName(String name) {
        this.methodName = name;
    }

    void setLineNumber(int number) {
        this.lineNumber = number;
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hashCode(getClassName())
             + ObjectUtil.hashCode(getMethodName())
             + getLineNumber()
             ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;

        SourceCodeLocation  other=(SourceCodeLocation) obj;
        return ObjectUtil.typedEquals(getClassName(), other.getClassName())
            && ObjectUtil.typedEquals(getMethodName(), other.getMethodName())
            && (getLineNumber() == other.getLineNumber())
             ;
    }

    @Override
    public String toString() {
        return getClassName() + "#" + getMethodName() + ":" + getLineNumber();
    }

}
