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
package com.ebupt.webjoin.insight.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Some extra reflection utilities that use {@link ReflectionUtils}. We do
 * no change {@link ReflectionUtils} since we want to maintain its copy-paste
 * nature from the original code
 */
public abstract class ExtraReflectionUtils extends ReflectionUtils {
    public static final Object[]    EMPTY_ARGS=new Object[0];
    public static final Class<?>[]  EMPTY_PARAMS={ };

    public static <T> T invoke(Method method, Object target, Class<T> retType) {
        return invoke(method, target, retType, EMPTY_ARGS);
    }

    /**
     * Invokes a {@link Method} and casts the return value to the provided type
     * @param <T> Return value type
     * @param method The {@link Method} to invoke
     * @param target Invocation target
     * @param retType Type to cast return value to 
     * @param args The invocation arguments
     * @return The (cast) return value of the invocation
     * @see #invokeMethod(Method, Object, Object...)
     * @see #castReturnValue(Object, Class)
     */
    public static <T> T invoke(Method method, Object target, Class<T> retType, Object ... args) {
        return castReturnValue(invokeMethod(method, target, args), retType);
    }

    /**
     * @param <T> Field value type
     * @param field The {@link Field} accessor
     * @param target Invocation target
     * @param fieldType The expected field value type
     * @return The field value cast to a requested type
     * @see #getField(Field, Object)
     * @see #castReturnValue(Object, Class)
     */
    public static <T> T getFieldValue(Field field, Object target, Class<T> fieldType) {
        return castReturnValue(getField(field, target), fieldType);
    }

    public static Field getAccessibleField(Class<?> clazz, String name) {
        return getAccessibleField(clazz, name, null);
    }

    /**
     * @param clazz The {@link Class} to be explored for the requested field
     * @param name the name of the field (may be <code>null</code> if type is specified)
     * @param type the type of the field (may be <code>null</code> if name is specified)
     * @return the corresponding {@link Field} object (or <code>null</code>
     * if not found) make accessible
     * @see #findField(Class, String, Class)
     * @see #makeAccessible(Field)
     */
    public static Field getAccessibleField(Class<?> clazz, String name, Class<?> type) {
        Field   field=findField(clazz, name, type);
        if (field == null) {
            return null;    // debug breakpoint
        }

        makeAccessible(field);
        return field;
    }

    public static Method getAccessibleMethod(Class<?> clazz, String name) {
        return getAccessibleMethod(clazz, name, EMPTY_PARAMS);
    }

    /**
     * @param clazz The {@link Class} to introspect
     * @param name The required method name
     * @param paramTypes Method parameters types
     * @return the {@link Method} object (or <code>null</code> if none found)
     * made accessible
     * @see #findMethod(Class, String, Class...)
     * @see #makeAccessible(Method)
     */
    public static Method getAccessibleMethod(Class<?> clazz, String name, Class<?> ... paramTypes) {
        Method  method=findMethod(clazz, name, paramTypes);
        if (method == null) {
            return null;    // debug breakpoint
        }

        makeAccessible(method);
        return method;
    }

    private static <T> T castReturnValue(Object returnValue, Class<T> retType) {
        if (returnValue == null) {
            return null;    // debug breakpoint
        }

        return retType.cast(returnValue);
    }
}
