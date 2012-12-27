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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.ebupt.webjoin.insight.intercept.LazyConstructor;
import com.ebupt.webjoin.insight.util.MapUtil;
import com.ebupt.webjoin.insight.util.ObjectUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

 

/**
 * An abstract representation of 'something being done'.  
 * An Operation could be:
 * <ul>
 *    <li>an HTTP GET request</li>
 *    <li>a java Class + Method</li>
 *    <li>a script to execute in the shell</li>
 * </ul>      
 *
 * <p>Operations are used in terms of describing what is
 * done as an application flows through its {@link Trace}.
 * <ul>
 *   <li>"I received an HTTP GET request at /index.html"</li>
 *   <li>"Then I called ListingController.getAds()"</li>
 *   <li>"Then I rendered the output"</li>
 * </ul>
 *   
 * <p>In each case, we are describing an operation.
 * 
 * <p>Operations are restricted in the types of objects they may contain.  
 * boolean, byte, char, Date, double, float, int, long, short and String are 
 * supported.  List and Map like data structures are supported via 
 * OperationList and OperationMap that apply the same basic type restrictions.
 * 
 * <p>A standardized Operation structure improves backwards and forwards 
 * compatibility between releases.  Plug-in authors no longer need to worry 
 * about serial compatibility.
 */
public final class Operation implements LazyConstructor, Serializable {
    
    private static final long serialVersionUID = 6734315511525682706L;
    
    private OperationType type;
    
    private String label;

    private SourceCodeLocation sourceCodeLocation;
    
    private Map<String, Object> properties;
    
    private transient List<OperationFinalizer> finalizers;
    
    private transient Map<String, Object> finalizerRichObjects;
    
    public Operation() {
        type = OperationType.SIMPLE;
        label = "";
        properties = new HashMap<String, Object>();
        finalizers = new ArrayList<OperationFinalizer>();
        finalizerRichObjects = new HashMap<String, Object>();
    }
    
    public Map<String, Object> asMap() {
        return asMap(null);
    }
    
    /**
     * Converts the operation to an immutable map.  This is commonly used to 
     * simplify data access for view rendering.
     */
    public Map<String, Object> asMap(OperationFieldVisitor visitor) {
        Map<String, Object> map = new HashMap<String, Object>(properties.size());
        for (Map.Entry<String,?> vp : properties.entrySet()) {
            String  key=vp.getKey();
            Object  value=vp.getValue();

            if (value instanceof OperationMapImpl) {
                value = ((OperationMapImpl) value).asMap();
            } else if (value instanceof OperationListImpl) {
                value = ((OperationListImpl) value).asList();
            }

            MapUtil.putIfNonNull(map, key, (visitor != null) ? visitor.visit(key, value) : value);
        }

        MapUtil.putIfNonNull(map, "type", getType());
        MapUtil.putIfNonNull(map, "label", getLabel());
        MapUtil.putIfNonNull(map, "sourceCodeLocation", getSourceCodeLocation());
        return Collections.unmodifiableMap(map);
    }

    /**
     * @return the operation type.  Defaults to OperationType.SIMPLE
     */
    public OperationType getType() {
        return type;
    }

    /**
     * Defines the type for the operation
     * 
     * @return the operation
     */
    public Operation type(OperationType typeValue) {
        setType(typeValue);
        return this;
    }

    public void setType(OperationType typeValue) {
        this.type = typeValue;
    }

    /**
     * @return the label for the operation
     */
    public String getLabel() {
        return label;
    }

    /**
     * Defines the label for the operation.  May be null.
     * 
     * @return the operation
     */
    public Operation label(String labelValue) {
        setLabel(labelValue);
        return this;
    }

    public void setLabel(String labelValue) {
        this.label = labelValue;
    }

    /**
     * @return the source code location the operation represents.  May be null.
     */
    public SourceCodeLocation getSourceCodeLocation() {
        return sourceCodeLocation;
    }

    /**
     * Defines the source code location for the operation
     * 
     * @return the operation
     */
    public Operation sourceCodeLocation(SourceCodeLocation scl) {
        setSourceCodeLocation(scl);
        return this;
    }

    public void setSourceCodeLocation(SourceCodeLocation scl) {
        this.sourceCodeLocation = scl;
    }

    /**
     * @return the value at the specified map key.
     */
    public Object get(String key) {
        return properties.get(key);
    }

    /**
     * @return the value at the specified map key, but only if it is an 
     * instance of the provided type.
     */
    public <C> C get(String key, Class<C> clazz) {
        return get(key, clazz, null);
    }

    /**
     * @return the value at the specified map key, but only if it is an 
     * instance of the provided type.  If the value for the key is not of the 
     * specified type or null, the default value is returned.
     */
    public <C> C get(String key, Class<C> clazz, C defaultValue) {
        Object property = get(key);
        if ((property != null) && clazz.isAssignableFrom(property.getClass())) {
            return clazz.cast(property);
        }
        return defaultValue;
    }

    /**
     * @param key The key value
     * @param clazz The {@link Enum}-s {@link Class}
     * @return The enumerated value - if the key has a {@link String} value,
     * <code>null</code> if the key does not exist or is not a string
     * @throws IllegalArgumentException if the mapped string value does not
     * represent a valid enumerated value of the provided class
     * @see #getEnum(String, Class, Enum)
     */
    public <E extends Enum<E>> E getEnum (String key, Class<E> clazz)
            throws IllegalArgumentException {
        return getEnum(key, clazz, null);
    }

    /**
     * @param key The key value
     * @param clazz The {@link Enum}-s {@link Class}
     * @param defaultValue The value to return if key does not exist or
     * is not a {@link String} value
     * @return The enumerated value - if the key has a {@link String} value,
     * the default value if the key does not exist or is not a string
     * @throws IllegalArgumentException if the mapped string value does not
     * represent a valid enumerated value of the provided class
     * @see Enum#valueOf(Class, String)
     */
    public <E extends Enum<E>> E getEnum (String key, Class<E> clazz, E defaultValue)
            throws IllegalArgumentException {
        String  name=get(key, String.class);
        if (StringUtil.isEmpty(name)) {
            return defaultValue;
        }

        return Enum.valueOf(clazz, name);
    }
    /**
     * @return the number of entries currently in the map
     */
    public int size() {
        return properties.size();
    }

    /**
     * @return the map keys
     */
    public Set<String> keySet() {
        return new TreeSet<String>(properties.keySet());
    }

    public List<Map.Entry<String,Object>> entrySet () {
        return new ArrayList<Map.Entry<String,Object>>(properties.entrySet());
    }

    private Operation doPut(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    /**
     * @param key Key to use
     * @param value Value to map
     * @return the <code>this</code> instance
     * @see OperationUtils#resolveOperationObject(Object) for the actual
     * mapped value
     */
    public Operation putAny (String key, Object value) {
        return doPut(key, OperationUtils.resolveOperationObject(value));
    }
    
    /**
     * @param key Key to use
     * @param value Value to map - if <code>null</code> or its
     * {@link OperationUtils#resolveOperationObject(Object)} result is
     * <code>null</code> then nothing is mapped. The same applies for
     * an empty {@link String} (i.e., one whose length is not &gt;0)
     * @return the <code>this</code> instance
     */
    public Operation putAnyNonEmpty (String key, Object value) {
        Object  effectiveValue=(value == null) ? null : OperationUtils.resolveOperationObject(value);
        if (effectiveValue == null) {
            return this;
        }

        if ((effectiveValue instanceof String) && (((String) effectiveValue).length() <= 0)) {
            return this;
        }

        return doPut(key, effectiveValue);
    }
    /**
     * Add a boolean to the map for the key
     * 
     * @return the operation
     */
    public Operation put(String key, boolean value) {
        return doPut(key, Boolean.valueOf(value));
    }

    /**
     * Add a byte to the map for the key
     * 
     * @return the operation
     */
    public Operation put(String key, byte value) {
        return doPut(key, Byte.valueOf(value));
    }

    /**
     * Add a char to the map for the key
     * 
     * @return the operation
     */
    public Operation put(String key, char value) {
        return doPut(key, Character.valueOf(value));
    }

    /**
     * Add a Date to the map for the key
     * 
     * @return the operation
     */
    public Operation put(String key, Date value) {
        return doPut(key, new Date(value.getTime()));
    }

    /**
     * Add a double to the map for the key
     * 
     * @return the operation
     */
    public Operation put(String key, double value) {
        return doPut(key, Double.valueOf(value));
    }

    /**
     * Add a float to the map for the key
     * 
     * @return the operation
     */
    public Operation put(String key, float value) {
        return doPut(key, Float.valueOf(value));
    }

    /**
     * Add a int to the map for the key
     * 
     * @return the operation
     */
    public Operation put(String key, int value) {
        return doPut(key, Integer.valueOf(value));
    }

    /**
     * Add a long to the map for the key
     * 
     * @return the operation
     */
    public Operation put(String key, long value) {
        return doPut(key, Long.valueOf(value));
    }

    /**
     * Add a short to the map for the key
     * 
     * @return the operation
     */
    public Operation put(String key, short value) {
        return doPut(key, Short.valueOf(value));
    }

    /**
     * Add an {@link Enum#name()} to the map for the key
     * 
     * @return the operation
     */
    public Operation put (String key, Enum<?> value) {
        return doPut(key, value.name());
    }

    /**
     * Add a String to the map for the key
     * 
     * @return the operation
     */
    public Operation put(String key, String value) {
        return doPut(key, value);
    }

    public Operation put(String key, OperationList list) {
        return doPut(key, list);
    }

    public Operation put(String key, OperationMap map) {
        return doPut(key, map);
    }

    /**
     * Copy all the properties from a source into this operation.
     *
     * Note that internal references will be shared between
     * the two Operations, hence updating an OperationList within
     * one will update it in the other.
     *
     * @return this operation
     */
    public Operation copyPropertiesFrom(Operation source) {
        properties.putAll(source.properties);
        return this;
    }

    /**
     * Create a child map in the current map for the key
     *   
     * @return the new map
     */
    public OperationMap createMap(String key) {
        OperationMap map = new OperationMapImpl();
        doPut(key, map);
        return map;
    }

    /**
     * Create a child list in the current map for the key
     *   
     * @return the new list
     */
    public OperationList createList(String key) {
        OperationListImpl list = new OperationListImpl();
        doPut(key, list);
        return list;
    }

    public void remove(String key) {
        this.properties.remove(key);
    }
    
    // finalization
    
    /**
     * Add a finalizer to be invoked when the operation construction is 
     * finalized.  Finalizers will be executed in the order defined.
     */
    public Operation addFinalizer(OperationFinalizer finalizer) {
        finalizers.add(finalizer);
        return this;
    }
    
    /**
     * Add an object to be provided to OperationFinalizers along with the 
     * operation.  The finalizer receives the objects as a Map with the value 
     * indexed under the provided key.
     */
    public Operation addFinalizerObject(String key, Object value) {
        finalizerRichObjects.put(key, value);
        return this;
    }

    public boolean isFinalizable() {
        synchronized (this) {
            return finalizers != null && !finalizers.isEmpty();
        }
    }

    public void finalizeConstruction() {
        synchronized (this) {
            if (finalizers == null || finalizers.isEmpty()) {
                return;
            }
            for (OperationFinalizer finalizer : finalizers) {
                finalizer.finalize(this, finalizerRichObjects);
            }
            finalizers = null;
            finalizerRichObjects = null;
        }
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        // ensure Operation is finalized before serialization
        if (isFinalizable()) {
            finalizeConstruction();
        }
        out.defaultWriteObject();
    }

    // Setter Methods package scope to allow for mutability in deserialization
    void setProperties(Map<String, Object> props) {
        this.properties = props;
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hashCode(getType())
             + ObjectUtil.hashCode(getSourceCodeLocation())
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

        /*
         * NOTE: we do not check the properties since some serializers (e.g.,
         * JSON) add more properties than we really need + it might prove
         * very in-efficient due to the "depth" of some of the properties
         */
        Operation   other=(Operation) obj;
        return ObjectUtil.typedEquals(getType(), other.getType())
            && ObjectUtil.typedEquals(getSourceCodeLocation(), other.getSourceCodeLocation())
             ;
    }

    @Override
    public String toString() {
        String  lbl=getLabel();
        return StringUtil.isEmpty(lbl) ? "Operation" : lbl;
    }
}
