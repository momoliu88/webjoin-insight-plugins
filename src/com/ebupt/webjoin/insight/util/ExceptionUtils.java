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

import java.lang.reflect.InvocationTargetException;

import javax.management.MBeanException;
import javax.management.ReflectionException;

public final class ExceptionUtils {
    private ExceptionUtils () {
        // no instance
    }
    /**
     * @param e The original {@link Throwable}
     * @return <P>A {@link RuntimeException} representing the original one:</P></BR>
     * <UL>
     *      <LI><code>null</code> if original was <code>null</code></LI>
     * 
     *      <LI>Same as input if input already a {@link RuntimeException}</LI>
     * 
     *      <LI>A {@link RuntimeException} encapsulating the original one</LI>
     * </UL>
     * @see #toRuntimeException(Throwable, boolean)
     */
    public static RuntimeException toRuntimeException (Throwable e) {
        return toRuntimeException(e, false);
    }
    /**
     * @param e The original {@link Throwable}
     * @param peelWrapper If TRUE then some special exceptions are &quot;peeled&quot;
     * to get at the real cause (e.g., {@link InvocationTargetException})
     * @return <P>A {@link RuntimeException} representing the original one:</P></BR>
     * <UL>
     *      <LI><code>null</code> if original was <code>null</code></LI>
     * 
     *      <LI>Same as input if input already a {@link RuntimeException}</LI>
     * 
     *      <LI>A {@link RuntimeException} encapsulating the original one</LI>
     * </UL>
     */
    public static RuntimeException toRuntimeException (Throwable e, boolean peelWrapper) {
        if (null == e)
            return null;
        if (e instanceof RuntimeException)
            return (RuntimeException) e;

        if (peelWrapper) {
        	Throwable	t=peelThrowable(e);
        	if (t != e) {
        		return toRuntimeException(t, false);
        	}
        }

        return new RuntimeException(e);
    }

    /**
     * Attempts to extract the &quot;real&quot; {@link Throwable} from some
     * known ones that are actually containers of others
     * @param e
     * @return
     */
    public static final Throwable peelThrowable (Throwable e) {
    	if (e instanceof InvocationTargetException) {
    		return peelThrowable(((InvocationTargetException) e).getTargetException());
    	} else if (e instanceof MBeanException) {
    		return peelThrowable(((MBeanException) e).getTargetException());
    	} else if (e instanceof ReflectionException) {
    		return peelThrowable(((ReflectionException) e).getTargetException());
    	} else {
    		return e;
    	}
    }
    /**
     * Provides a way to re-throw any {@link Throwable} without having to
     * declare the caller as <I>throws</I> that exception. Based on
     * <A HREF="http://java.dzone.com/articles/throwing-undeclared-checked">this article</A>
     * @param t The {@link Throwable} instance to throw
     */
    @SuppressWarnings("synthetic-access")
    public static void rethrowException (Throwable t) {
        ExceptionThrower.spit(t);
    }

    private static final class ExceptionThrower {
        private static Throwable throwable;
        @SuppressWarnings("unused")
        public ExceptionThrower () throws Throwable {
            if (throwable != null) {    // just a safety
                throw throwable;
            }
        }

        private static synchronized void spit (Throwable t) {
            if (t == null) {
                throw new IllegalArgumentException("No throwable to rethrow");
            }
            
            ExceptionThrower.throwable = t;
            try {
                ExceptionThrower.class.newInstance();
            } catch(InstantiationException e) {
                throw new IllegalStateException("spit(" + t.getClass().getSimpleName() + ")"
                                              + " unexpected instantiation exception: " + e.getMessage());
            } catch(IllegalAccessException e) {
                throw new IllegalStateException("spit(" + t.getClass().getSimpleName() + ")"
                                              + " unexpected access exception: " + e.getMessage());
            } finally {
                ExceptionThrower.throwable = null;
            }
        }

    }
}
