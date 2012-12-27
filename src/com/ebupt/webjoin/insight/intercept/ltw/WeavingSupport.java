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

import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import com.ebupt.webjoin.insight.util.ListUtil;


/**
 * Some helper methods for weaving via AspectJ
 */
public final class WeavingSupport {
    private WeavingSupport() {
        // No instance
    }

    /**
     * An implementation of an &quot;empty&quot; {@link Enumeration} of {@link URL}-s
     * that reports <code>false</code> on {@link Enumeration#hasMoreElements()} and
     * throws a {@link NoSuchElementException} if {@link Enumeration#nextElement()}
     * is invoked. This is used to replace returned values for {@link ClassLoader#getResources(String)}
     * in some weaving aspects
     */
    public static final Enumeration<URL>    EMPTY_URL_ENUMERATION=new Enumeration<URL>() {
        /*
         * @see java.util.Enumeration#hasMoreElements()
         */
        public boolean hasMoreElements() {
            return false;
        }
        /*
         * @see java.util.Enumeration#nextElement()
         */
        public URL nextElement() {
            throw new NoSuchElementException("Unexpected call to next-element");
        }
    };

    // see org.aspectj.bridge.CONSTANTS
    /* Default resource names for user and generate aop.xml file */
    public final static String AOP_USER_XML = "META-INF/aop.xml";
    public final static String AOP_AJC_XML = "META-INF/aop-ajc.xml";
    /* Resource name for OSGi */
    public final static String AOP_OSGI_XML = "org/aspectj/aop.xml";

    // see ClassLoaderWeavingAdaptor#parseDefinitions
    private final static String AOP_XML=AOP_USER_XML + ";" + AOP_AJC_XML + ";" + AOP_OSGI_XML;
    private static final Set<String>    _aopResources;
    static {
        final String        resourcePath=System.getProperty("org.aspectj.weaver.loadtime.configuration", AOP_XML);
        final Set<String>   resources=new TreeSet<String>();
        for (final StringTokenizer st = new StringTokenizer(resourcePath, ";"); st.hasMoreTokens(); ) {
            final String nextDefinition = st.nextToken();
            if (nextDefinition.startsWith("file:")) {
                // TODO log or throw an exception
                continue;
            }
            if (!resources.add(nextDefinition)) {
                // TODO log or throw an exception
                continue;
            }
        }
        _aopResources = Collections.unmodifiableSet(resources);
    }
    /**
     * @return An unmodifiable {@link Set} of the AOP resources used for class loader lookup
     */
    public static final Set<String> getAOPResources () {
        return _aopResources;
    }

    /**
     * @param name A resource name
     * @return TRUE if the requested name is an AOP configuration file resource
     */
    public static final boolean isAopResource(final String name) {
        if ((name == null) || (name.length() <= 0)) {
            return false;   // we care only about non-null/empty resources
        }

        if (_aopResources.contains(name)) {
            return true;    
        }

        return false;
    }

    /**
     * @param name Original requested resource name - if not an AOP resource
     * then nothing is done
     * @param resources Returned resources {@link Enumeration} of {@link URL}-s
     * @param comp {@link Comparator} to use to decide whether a {@link URL} is
     * repeated in the results enumeration
     * @return An equivalent {@link Enumeration} where any duplicate {@link URL}-s
     * as determined by the supplied {@link Comparator} have been removed.
     * <B>Note:</B> does not preserve the original order of the values enumeration.
     */
    @SuppressWarnings("unchecked")
	public static final Enumeration<URL> eliminateDuplicateAopResources (
            final String name, final Enumeration<URL> resources, final Comparator<? super URL> comp) {
        if ((!isAopResource(name)) || (resources == null)) {
            return resources;
        }

        return ListUtil.eliminateDuplicates(resources, (Comparator<URL>)comp);
    }
}
