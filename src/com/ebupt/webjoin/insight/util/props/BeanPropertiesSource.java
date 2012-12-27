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

package com.ebupt.webjoin.insight.util.props;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import com.ebupt.webjoin.insight.intercept.util.KeyValPair;
import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.ClassUtil;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.MapUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

 

/**
 * Provides a {@link NamedPropertySource} access to a bean via introspection.
 */
public class BeanPropertiesSource  {
	/**
	 * Placeholder used if bean class not found and this should be ignored
	 */
	private static final KeyValPair<Class<?>,Map<String,Method>> EMPTY_ATTRIBUTES=
			new KeyValPair<Class<?>, Map<String,Method>>(Object.class, Collections.<String,Method>emptyMap());

	private final Class<?>	clazz;
	private final Map<String,Method> attrsMap;
	private final Set<String> attrsNames;

	public BeanPropertiesSource(Class<?> beanClass) throws IntrospectionException {
		this(beanClass, getAttributesMap(beanClass));
	}

	public BeanPropertiesSource(String beanClass, boolean ignoreIfNotPresent) {
		this(beanClass, ClassUtil.getDefaultClassLoader(), ignoreIfNotPresent);
	}

	public BeanPropertiesSource(String beanClass, Class<?> anchor, boolean ignoreIfNotPresent) {
		this(beanClass, ClassUtil.getDefaultClassLoader(anchor), ignoreIfNotPresent);
	}

	public BeanPropertiesSource(String beanClass, ClassLoader cl, boolean ignoreIfNotPresent) {
		this(getAttributesMap(beanClass, cl, ignoreIfNotPresent));
	}

	public BeanPropertiesSource(KeyValPair<Class<?>,Map<String,Method>> kvp) {
		this(kvp.getKey(), kvp.getValue());
	}

	public BeanPropertiesSource(Class<?> beanClass, Map<String,Method> accsMap) {
		if ((clazz=beanClass) == null) {
			throw new IllegalStateException("No bean class specified");
		}

		attrsMap = (accsMap == null)
				 ? Collections.<String,Method>emptyMap()
				 : Collections.unmodifiableMap(accsMap)
				 ;
		attrsNames = Collections.unmodifiableSet((MapUtil.size(accsMap) <= 0)
								? Collections.<String>emptySet()
								: accsMap.keySet())
								;
	}

	public final Class<?> getBeanClass () {
		return clazz;
	}

	/**
	 * Retrieves all current <U>non-<code>null</code></U> properties from the specified bean instance
	 * @param target The bean instance
	 * @return A {@link Map} whose key=the attribute name (case <U>insensitive</U>) and
	 * value=the retrieved (non-<code>null</code>) value
	 * @throws Exception If failed to retrieve an attribute
	 */
	public Map<String,Object> getAllProperties (Object target) throws Exception {
		return getProperties(target, getPropertyNames());
	}
	/**
	 * Retrieves all <U>non-<code>null</code></U> properties from the specified bean instance
	 * @param target The bean instance
	 * @param props The properties names - may be <code>null</code>/empty
	 * @return A {@link Map} whose key=the attribute name (case <U>insensitive</U>) and
	 * value=the retrieved (non-<code>null</code>) value
	 * @throws Exception If failed to retrieve an attribute
	 */
	public Map<String,Object> getProperties (Object target, String ... props) throws Exception {
		return getProperties(target, (ArrayUtil.length(props) <= 0) ? Collections.<String>emptyList() : Arrays.asList(props));
	}

	/**
	 * Retrieves all <U>non-<code>null</code></U> properties from the specified bean instance
	 * @param target The bean instance
	 * @param props The properties names - may be <code>null</code>/empty
	 * @return A {@link Map} whose key=the attribute name (case <U>insensitive</U>) and
	 * value=the retrieved (non-<code>null</code>) value
	 * @throws Exception If failed to retrieve an attribute
	 */
	public Map<String,Object> getProperties (Object target, Collection<String> props) throws Exception {
		if (target == null) {
			throw new IllegalArgumentException("No target provided");
		}

		Class<?> beanClass=getBeanClass(), targetClass=target.getClass();
		if (!beanClass.isAssignableFrom(targetClass)) {
			throw new IllegalArgumentException("getProperties(" + props + ")"
											 + " target class (" + targetClass.getName() + ")"
											 + " is incompatible with bean class (" + beanClass.getName() + ")");
		}

		if (ListUtil.size(props) <= 0) {
			return Collections.emptyMap();
		}

		Map<String,Object>	result=new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		for (String name : props) {
			if (StringUtil.isEmpty(name)) {
				throw new IllegalArgumentException("getProperties(" + props + ") - null/empty property name specified");
			}

			if (result.containsKey(name)) {
				continue;	// skip duplicates
			}
			
			Method	getter=attrsMap.get(name);
			Object	value=(getter == null) ? null : getter.invoke(target, ArrayUtil.EMPTY_OBJECTS);
			if (value == null) {
				continue;	// ignore null/non-existing values
			}

			result.put(name, value);
		}

		return result;
	}

	public Object getProperty(Object target, String name, Object defaultValue) throws Exception {
		Object	value=getProperty(target, name);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	/**
	 * Retrieves a given property only if it exists and is compatible with a
	 * required type
	 * @param target Target bean instance
	 * @param name Attribute name
	 * @param attrType Required attribute type
	 * @return The attribute value - <code>null</code> if no such property, or
	 * property value not compatible with required type or property value is
	 * <code>null</code> to begin with
	 * @throws Exception If failed retrieve property value
	 * @see #getProperty(Object, String)
	 */
	public <T> T getProperty (Object target, String name, Class<T> attrType) throws Exception {
		if (attrType == null) {
			throw new IllegalArgumentException("No required attribute type specified");
		}

		Object	value=getProperty(target, name);
		if (value == null) {
			return null;
		}

		if (!attrType.isAssignableFrom(value.getClass())) {
			return null;
		} else {
			return attrType.cast(value);
		}
	}

	public Object getProperty (Object target, String name) throws Exception {
		if (target == null) {
			throw new IllegalArgumentException("No target provided");
		}

		Class<?> beanClass=getBeanClass(), targetClass=target.getClass();
		if (!beanClass.isAssignableFrom(targetClass)) {
			throw new IllegalArgumentException("getProperty(" + name + ")"
											 + " target class (" + targetClass.getName() + ")"
											 + " is incompatible with bean class (" + beanClass.getName() + ")");
		}

		if (StringUtil.isEmpty(name)) {
			throw new IllegalArgumentException("No property name specified");
		}

		Method	getter=attrsMap.get(name);
		if (getter == null) {
			return null;	// no such attribute
		} else {
			return getter.invoke(target, ArrayUtil.EMPTY_OBJECTS);
		}
	}

	public Collection<String> getPropertyNames() {
		return attrsNames;
	}

	/**
	 * @param name Property name (case <U>insensitive</U>)
	 * @return Property type - <code>null</code> if property does not exist
	 */
	public Class<?> getPropertyType (String name) {
		if (StringUtil.isEmpty(name)) {
			throw new IllegalArgumentException("No property name specified");
		}
	
		Method	getter=attrsMap.get(name);
		if (getter == null) {
			return null;	// no such attribute
		} else {
			return getter.getReturnType();
		}
	}

	/**
	 * Dynamic class loading and introspection on a bean class
	 * @param beanClassName The fully-qualified bean class name
	 * @param cl The {@link ClassLoader} to use
	 * @param ignoreIfNotPresent If <code>true</code> and failed to load the
	 * class, returns a non-<code>null</code> result containing the {@link Object}
	 * class and an empty attributes map
	 * @return A {@link KeyValPair} whose key is the bean class and value is a
	 * {@link Map} whose key=the attribute name (case <U>insensitive</U>) and
	 * value is the {@link Method} that can be used to retrieve the
	 * attribute's value
	 * @throws NoSuchElementException If class not found and <code>ignoreNotPresent</code>
	 * is <code>false</code>
	 * @throws UnsupportedOperationException If introspection failed
	 * @see #getAttributesMap(Class)
	 */
	public static final KeyValPair<Class<?>,Map<String,Method>> getAttributesMap (
				String beanClassName, ClassLoader cl, boolean ignoreIfNotPresent)
			throws NoSuchElementException, UnsupportedOperationException {
		final Class<?>	beanClass;
		try {
			beanClass = ClassUtil.loadClassByName(cl, beanClassName);
		} catch(ClassNotFoundException e) {
			if (ignoreIfNotPresent) {
				return EMPTY_ATTRIBUTES;
			} else {
				throw new NoSuchElementException("getAttributesMap(" + beanClassName + ") no such class");
			}
		}

		try {
			Map<String,Method>	attrsMap=getAttributesMap(beanClass);
			return new KeyValPair<Class<?>, Map<String,Method>>(beanClass, attrsMap);
		} catch(IntrospectionException e) {
			throw new UnsupportedOperationException("getAttributesMap(" + beanClassName + ")"
												  + " failed (" + e.getClass().getName() + ")"
												  + " to introspect: " + e.getMessage());
		}
	}

	/**
	 * @param beanClass The bean {@link Class} to introspect
	 * @return A {@link Map} whose key=the attribute name (case <U>insensitive</U>)
	 * and value is the {@link Method} that can be used to retrieve the
	 * attribute's value
	 * @throws IntrospectionException If failed to introspect the bean or duplicate
	 * getters for same attribute name  (case <U>insensitive</U>)
	 */
	public static final Map<String,Method> getAttributesMap (Class<?> beanClass) throws IntrospectionException {
		if (beanClass.isAnnotation()) {
			return getAnnotationAttributes(beanClass);
		}

		BeanInfo				beanInfo=Introspector.getBeanInfo(beanClass);
		PropertyDescriptor[]	props=beanInfo.getPropertyDescriptors();
		if (ArrayUtil.length(props) <= 0) {
			return Collections.emptyMap();
		}
		
		Map<String,Method>	attrsMap=new TreeMap<String, Method>(String.CASE_INSENSITIVE_ORDER);
		for (PropertyDescriptor desc : props) {
			String	name=desc.getName();
			if ("class".equalsIgnoreCase(name)) {
				continue;	// we supply the class value ourselves
			}

			Method	getter=desc.getReadMethod();
			if (getter == null) {
				continue;	// we are interested only in readable methods
			}

			Method	prev=attrsMap.put(name, getter);
			if (prev != null) {
				throw new IntrospectionException("Ambiguous getter(s) for " + name + ": " + getter + " / " + prev);
			}
		}

		return attrsMap;
	}

	public static final Map<String,Method> getAnnotationAttributes (Class<?> annClass) throws IntrospectionException {
		Collection<Method>	baseMethods=ListUtil.asSet(Annotation.class.getMethods());
		Method[]			annMethods=annClass.getMethods();
		Map<String,Method>	attrsMap=new TreeMap<String, Method>(String.CASE_INSENSITIVE_ORDER);

		for (Method m : annMethods) {
			String	name=m.getName();
			if ("class".equalsIgnoreCase(name)) {
				continue;	// we supply the class value ourselves
			}

			if (baseMethods.contains(m)) {
				continue;	// ignore the base class methods
			}

			Class<?>[]	params=m.getParameterTypes();
			if (ArrayUtil.length(params) != 0) {
				continue;	// annotations have only get-ters
			}


			Method	prev=attrsMap.put(name, m);
			if (prev != null) {
				throw new IntrospectionException("Ambiguous attribute(s) for " + name + ": " + m + " / " + prev);
			}
		}

		return attrsMap;
	}
}
