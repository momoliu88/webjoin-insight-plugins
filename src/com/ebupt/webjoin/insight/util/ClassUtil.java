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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;


/**
 * 
 */
public final class ClassUtil {
	/**
	 * Default static (final) method used by {@link #getInstance(Class)}
	 */
	public static final String	DEFAULT_INSTANCE_METHOD="getInstance";

    private ClassUtil() {
        throw new UnsupportedOperationException("No instance allowed");
    }

    /**
     * Determine whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return <code>false</code> if either the class or
     * one of its dependencies is not present or cannot be loaded using the default
     * class loader.
     * @param fqcn The fully qualified class name - may NOT be null/empty
     * @return whether the specified class is present
     * @throws IllegalArgumentException if <code>null</code>empty class name specified
     * @see #getDefaultClassLoader()
     * @see #isPresent(String, ClassLoader)
     */
    public static boolean isPresent(String fqcn) throws IllegalArgumentException {
        return isPresent(fqcn, getDefaultClassLoader());
    }

    /**
     * Determine whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return <code>false</code> if either the class or
     * one of its dependencies is not present or cannot be loaded using the default
     * class loader.
     * @param fqcn The fully qualified class name - may NOT be null/empty
     * @param anchor The anchor {@link Class} to use in case no current thread context loader
     * @return whether the specified class is present
     * @throws IllegalArgumentException if <code>null</code>empty class name specified
     * @see #getDefaultClassLoader(Class)
     * @see #isPresent(String, ClassLoader)
     */
    public static boolean isPresent(String fqcn, Class<?> anchor) throws IllegalArgumentException {
        return isPresent(fqcn, getDefaultClassLoader(anchor));
    }

    /**
     * Determine whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return <code>false</code> if either the class or
     * one of its dependencies is not present or cannot be loaded.
     * @param fqcn The fully qualified class name - may NOT be null/empty
     * @param classLoader The {@link ClassLoader} to use - may NOT be null/empty
     * @return whether the specified class is present
     * @see #loadClassByName(ClassLoader, String)
     * @throws IllegalArgumentException if no loader or <code>null</code>empty
     * class name specified
     */
    public static boolean isPresent(String fqcn, ClassLoader classLoader)
            throws IllegalArgumentException {
        try {
            return (loadClassByName(classLoader, fqcn) != null);
        } catch (ClassNotFoundException e) {
            // Class or one of its dependencies is not present...
            return false;
        }
    }

    /**
     * Used as a replacement for {@link Class#forName(String)} call in order
     * to provide a centralized location for loading classes by name. This
     * method uses the {@link #getDefaultClassLoader()} call result in order
     * to load the requested class
     * @param fqcn The fully qualified class name - may NOT be null/empty
     * @return The loaded {@link Class} instance
     * @throws IllegalArgumentException if  <code>null</code>empty class name
     * specified
     * @throws ClassNotFoundException If cannot load the class.
     * @see #loadClassByName(ClassLoader, String)
     */
    public static Class<?> loadClassByName (String fqcn)
           throws IllegalArgumentException, ClassNotFoundException {
        return loadClassByName(getDefaultClassLoader(), fqcn);
    }

    /**
     * Used as a replacement for {@link Class#forName(String)} call in order
     * to provide a centralized location for loading classes by name. This
     * method uses the {@link #getDefaultClassLoader()} call result in order
     * to load the requested class
     * @param fqcn The fully qualified class name - may NOT be null/empty
     * @param anchor The anchor {@link Class} to use in case no current thread context loader
     * @return The loaded {@link Class} instance
     * @throws IllegalArgumentException if  <code>null</code>empty class name
     * specified
     * @throws ClassNotFoundException If cannot load the class.
     * @see #loadClassByName(ClassLoader, String)
     * @see #getDefaultClassLoader(Class)
     */
    public static Class<?> loadClassByName (String fqcn, Class<?> anchor)
           throws IllegalArgumentException, ClassNotFoundException {
        return loadClassByName(getDefaultClassLoader(anchor), fqcn);
    }

    /**
     * Used as a replacement for {@link Class#forName(String, boolean, ClassLoader)} call
     * in order to provide a centralized location for loading classes by name. This
     * method uses the provided {@link ClassLoader} in order to load the requested class.
     * <B>Note:</B> the built-in <U>primitive</U> classes are resolved using the
     * {@link #getPrimitiveClass(String)} and not loaded via the specified loader
     * @param cl The {@link ClassLoader} instance to use - <B>Note:</B> must not
     * @param fqcn The fully qualified class name - may NOT be null/empty even if
     * it is not used (e.g., when requesting primitive types)
     * @return The loaded {@link Class} instance
     * @throws IllegalArgumentException if no loader or <code>null</code>empty
     * class name specified
     * @throws ClassNotFoundException If cannot load the class.
     * @see #loadClassByName(ClassLoader, String)
     */
    public static Class<?> loadClassByName (ClassLoader cl, String fqcn)
            throws IllegalArgumentException, ClassNotFoundException {
        if (StringUtil.isEmpty(fqcn)) {
            throw new IllegalArgumentException("No fully qualified class name specified");
        }
        if (cl == null) {
            throw new IllegalArgumentException("loadClassByName(" + fqcn + ") no loader specified");
        }

        Class<?>  pClass=getPrimitiveClass(fqcn);
        if (pClass != null) {
            return pClass;
        }

        return cl.loadClass(fqcn);
    }

    public static Manifest loadContainerManifest (Class<?> anchor) throws IOException {
        URL     classBytesURL=getClassBytesURL(anchor);
        String  scheme=(classBytesURL == null) ? null : classBytesURL.getProtocol();
        if (StringUtil.isEmpty(scheme) || (!scheme.startsWith("jar"))) {
            return null;
        }

        String      classPath=classBytesURL.toString(),
                    manifestPath=classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        URL         url=new URL(manifestPath);
        InputStream in=url.openStream();
        try {
            return new Manifest(in);
        } finally {
            in.close();
        }
    }

    /**
     * Convenience <I>varargs</I> method for {@link Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)}
     * using the {@link #getDefaultClassLoader()} result as the proxy loader
     * @param resultType The proxy result {@link Class} to be cast to
     * @param h The target {@link InvocationHandler}
     * @param interfaces The interface classes being proxy-ied
     * @return The create proxy
     * @see #newProxyInstance(Class, ClassLoader, InvocationHandler, Class...)
     */
    public static <T> T newProxyInstance(Class<T> resultType,InvocationHandler h, Class<?> ... interfaces) {
        return newProxyInstance(resultType, getDefaultClassLoader(), h, interfaces);
    }

    /**
     * Convenience <I>varargs</I> method for {@link Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)}
     * @param resultType The proxy result {@link Class} to be cast to
     * @param loader The {@link ClassLoader} to use
     * @param h The target {@link InvocationHandler}
     * @param interfaces The interface classes being proxy-ied
     * @return The create proxy
     * @see Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)
     */
    public static <T> T newProxyInstance(Class<T> resultType, ClassLoader loader, InvocationHandler h, Class<?> ... interfaces) {
        if (ArrayUtil.length(interfaces) <= 0) {
            throw new IllegalArgumentException("No interfaces specified");
        }

        for (Class<?> i : interfaces) {
            if (i == null) {
                throw new IllegalArgumentException("Blank spot in " + Arrays.toString(interfaces));
            }
            if (!i.isInterface()) {
                throw new IllegalArgumentException("Not an interface: " + i.getSimpleName());
            }
        }

        return resultType.cast(Proxy.newProxyInstance(loader, interfaces, h));
    }

    /**
     * @return A {@link ClassLoader} to be used by the caller. The loader is
     * resolved in the following manner:</P></BR>
     * <UL>
     *      <LI>
     *      If a non-<code>null</code> loader is returned from the
     *      {@link Thread#getContextClassLoader()} call then use it.
     *      </LI>
     *      
     *      <LI>
     *      Otherwise, use the same loader that was used to load this class.
     *      </LI>
     * </UL>
     * @see #getDefaultClassLoader(Class)
     */
    public static ClassLoader getDefaultClassLoader() {
        return getDefaultClassLoader(ClassUtil.class);
    }
    
    /**
     * @param anchor An &quot;anchor&quot; {@link Class} to be used in case
     * no thread context loader is available
     * @return A {@link ClassLoader} to be used by the caller. The loader is
     * resolved in the following manner:</P></BR>
     * <UL>
     *      <LI>
     *      If a non-<code>null</code> loader is returned from the
     *      {@link Thread#getContextClassLoader()} call then use it.
     *      </LI>
     *      
     *      <LI>
     *      Otherwise, use the same loader that was used to load the anchor class.
     *      </LI>
     * </UL>
     * @throws IllegalArgumentException if no anchor class provided (regardless of
     * whether it is used or not) 
     */
    public static ClassLoader getDefaultClassLoader(Class<?> anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("No anchor class provided");
        }

        Thread      t=Thread.currentThread();
        ClassLoader cl=t.getContextClassLoader();
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = anchor.getClassLoader();
        }

        if (cl == null) {	// no class loader - assume system
        	cl = ClassLoader.getSystemClassLoader();
        }

        return cl;
        
    }
    /**
     * @param clazz A {@link Class} instance
     * @return <code>true</code> if the class is one of the primitive type <U>wrappers</U>
     * (except for {@link Void})
     * @see #PRIMITIVE_TYPE_WRAPPERS
     */
    public static boolean isPrimitiveWrapperClass (Class<?> clazz) {
        return (clazz != null) && PRIMITIVE_TYPE_WRAPPERS.contains(clazz);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final List<Class<?>>  PRIMITIVE_TYPE_WRAPPERS=
            Collections.unmodifiableList((List)  // Note: the order is intended to provide efficient contains check
                    Arrays.asList(Integer.class,
                                  Long.class,
                                  Double.class,
                                  Boolean.class,
                                  Byte.class,
                                  Short.class,
                                  Float.class,
                                  Character.class));

    /**
     * @param clazz A {@link Class} instance
     * @return <code>true</code> if the class is one of the primitive types
     * (except for {@link Void})
     * @see Class#isPrimitive()
     */
    public static boolean isPrimitiveClass (Class<?> clazz) {
        return (clazz != null) && (clazz != Void.TYPE) && clazz.isPrimitive();
    }

    /**
     * @param dataType A data type class name
     * @return TRUE if this (non-null/empty) string represents one of the
     * primitive data types (the TYPE special class - e.g., <I>int</I>,<I>long</I>)
     * @see #getPrimitiveClass(String)
     */
    public static boolean isPrimitiveClass (String dataType) {
        return (getPrimitiveClass(dataType) != null);
    }

    /**
     * @param dataType The (potential) primitive data type
     * @return The <code>TYPE</code> special class - e.g., <I>int</I>,<I>long</I>
     * - <code>null</code> if the specified data type is <code>null</code>,
     * empty or does not refer to a primitive data type
     * @see #PRIMITIVE_TYPE_CLASSES
     */
    public static Class<?> getPrimitiveClass (String dataType) {
        if (StringUtil.isEmpty(dataType))
            return null;

        for (Class<?> pType : PRIMITIVE_TYPE_CLASSES) {
            if (dataType.equals(pType.getName()))
                return pType;
        }

        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final List<Class<?>>  PRIMITIVE_TYPE_CLASSES=
            Collections.unmodifiableList((List)  // Note: the order is intended to provide efficient contains check
                    Arrays.asList(Integer.TYPE,
                                  Long.TYPE,
                                  Double.TYPE,
                                  Boolean.TYPE,
                                  Byte.TYPE,
                                  Short.TYPE,
                                  Float.TYPE,
                                  Character.TYPE));
    /**
     * @param clazz A {@link Class} object
     * @return A {@link File} of the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     * @throws IllegalArgumentException If location is not a valid
     * {@link File} location
     * @see #getClassContainerLocationURI(Class)
     * @see File#File(URI) 
     */
    public static File getClassContainerLocationFile (Class<?> clazz)
            throws IllegalArgumentException {
        try {
            URI uri=getClassContainerLocationURI(clazz);
            return (uri == null) ? null : new File(uri);
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * @param clazz A {@link Class} object
     * @return A {@link URI} to the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     * @throws URISyntaxException if location is not a valid URI
     * @see #getClassContainerLocationURL(Class)
     */
    public static URI getClassContainerLocationURI (Class<?> clazz) throws URISyntaxException {
        URL url=getClassContainerLocationURL(clazz);
        return (url == null) ? null : url.toURI();
    }

    /**
     * @param clazz A {@link Class} object
     * @return A {@link URL} to the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     */
    public static URL getClassContainerLocationURL (Class<?> clazz) {
        ProtectionDomain    pd=clazz.getProtectionDomain();
        CodeSource          cs=(pd == null) ? null : pd.getCodeSource();
        URL					url=(cs == null) ? null : cs.getLocation();
        if (url == null) {
        	ClassLoader	cl=getDefaultClassLoader(clazz);
        	String		className=clazz.getName().replace('.', '/') + ".class";
        	if ((url=cl.getResource(className)) == null) {
        		return null;
        	}
        	
        	String	srcForm=FileUtil.getURLSource(url);
        	if (StringUtil.isEmpty(srcForm)) {
        		return null;
        	}

        	try {
        		url = new URL(srcForm);
        	} catch(MalformedURLException e) {
        		throw new IllegalArgumentException("getClassContainerLocationURL(" + clazz.getName() + ")"
        										  + "Failed to create URL=" + srcForm + " from " + url.toExternalForm()
        										  + ": " + e.getMessage());
        	}
        }

        return url;
    }
    
    /**
     * @param clazz The request {@link Class}
     * @return A {@link URL} to the location of the <code>.class</code> file
     * - <code>null</code> if location could not be resolved
     */
    public static URL getClassBytesURL (Class<?> clazz) {
        return clazz.getResource(clazz.getSimpleName() + ".class");
    }
    
    /**
     * Attempts to instantiate a give class by 1st looking for a (static)
     * instantiation method. If such a method is found, then it is invoked,
     * otherwise {@link Class#newInstance()} is called
     * @param clazz The {@link Class} type to be instantiated
     * @return The instance value
     * @throws Exception If failed to instantiate
     * @see #DEFAULT_INSTANCE_METHOD
     * @see #getInstance(Class, String)
     */
    public static <T> T getInstance (Class<T> clazz) throws Exception {
    	return getInstance(clazz, DEFAULT_INSTANCE_METHOD);
    }

    /**
     * Attempts to instantiate a give class by 1st looking for a (static)
     * instantiation method. If such a method is found, then it is invoked,
     * otherwise {@link Class#newInstance()} is called.
     * @param methodName The (<code>public static <U>final</U></code>) method
     * name to look for as instantiator. <B>Note:</B> if method is found but
     * is not <code>public</code> or <code>static</code> or <code>final</code>,
     * or the invocation result is <code>null</code> then exception is thrown.
     * @param clazz The {@link Class} type to be instantiated
     * @return The instance value
     * @throws Exception If failed to instantiate
     */
    public static <T> T getInstance (Class<T> clazz, String methodName) throws Exception {
    	Method	instanceMethod=ReflectionUtils.findMethod(clazz, methodName, ArrayUtil.EMPTY_CLASSES);
    	if (instanceMethod == null) {
    		return clazz.newInstance();
    	}

    	int	mod=instanceMethod.getModifiers();
    	if (!Modifier.isPublic(mod)) {
    		throw new IllegalAccessException("getInstance(" + clazz.getName() + ")#" + methodName + " - not public");
    	}
    	if (!Modifier.isStatic(mod)) {
    		throw new IllegalAccessException("getInstance(" + clazz.getName() + ")#" + methodName + " - not static");
    	}

    	if (!Modifier.isFinal(mod)) {
    		throw new IllegalAccessException("getInstance(" + clazz.getName() + ")#" + methodName + " - not final");
    	}

    	Object	result=ReflectionUtils.invokeStaticMethod(instanceMethod);
    	if (result == null) {
    		throw new IllegalAccessException("getInstance(" + clazz.getName() + ")#" + methodName + " - result is null");
    	}

    	return clazz.cast(result);
    }
}
