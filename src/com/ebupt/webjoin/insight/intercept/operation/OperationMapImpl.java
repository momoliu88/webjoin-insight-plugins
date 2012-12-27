package com.ebupt.webjoin.insight.intercept.operation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import com.ebupt.webjoin.insight.json.InsightJsonObject;

final class OperationMapImpl implements OperationMap, Serializable {
	private static final long serialVersionUID = -610742646758439028L;
	private Map<String, Object> properties;

	OperationMapImpl() {
		this.properties = new HashMap<String, Object>();
	}

	public Object get(String key) {
		return this.properties.get(key);
	}

	public <C> C get(String key, Class<C> clazz) {
		return get(key, clazz, null);
	}

	@SuppressWarnings("unchecked")
	public <C> C get(String key, Class<C> clazz, C defaultValue) {
		Object property = get(key);
		if ((property != null) && (clazz.isAssignableFrom(property.getClass()))) {
			return (C) property;
		}
		return defaultValue;
	}

	public int size() {
		return this.properties.size();
	}

	public Set<String> keySet() {
		return this.properties.keySet();
	}

	private OperationMap doPut(String key, Object value) {
		this.properties.put(key, value);
		return this;
	}

	public OperationMap putAny(String key, Object value) {
		return doPut(key, OperationUtils.resolveOperationObject(value));
	}

	public OperationMap putAnyNonEmpty(String key, Object value) {
		Object effectiveValue = (value == null) ? null : OperationUtils
				.resolveOperationObject(value);
		if (effectiveValue == null) {
			return this;
		}

		if (((effectiveValue instanceof String))
				&& (((String) effectiveValue).length() <= 0)) {
			return this;
		}

		return doPut(key, effectiveValue);
	}

	public OperationMap put(String key, boolean value) {
		return doPut(key, Boolean.valueOf(value));
	}

	public OperationMap put(String key, byte value) {
		return doPut(key, Byte.valueOf(value));
	}

	public OperationMap put(String key, char value) {
		return doPut(key, Character.valueOf(value));
	}

	public OperationMap put(String key, Date value) {
		return doPut(key, new Date(value.getTime()));
	}

	public OperationMap put(String key, double value) {
		return doPut(key, Double.valueOf(value));
	}

	public OperationMap put(String key, float value) {
		return doPut(key, Float.valueOf(value));
	}

	public OperationMap put(String key, int value) {
		return doPut(key, Integer.valueOf(value));
	}

	public OperationMap put(String key, long value) {
		return doPut(key, Long.valueOf(value));
	}

	public OperationMap put(String key, short value) {
		return doPut(key, Short.valueOf(value));
	}

	public OperationMap put(String key, String value) {
		return doPut(key, value);
	}

	public OperationMap createMap(String key) {
		OperationMap map = new OperationMapImpl();
		doPut(key, map);
		return map;
	}

	public OperationList createList(String key) {
		OperationList list = new OperationListImpl();
		doPut(key, list);
		return list;
	}
	@Override
	public Map<String, Object> asMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		for (String key : keySet()) {
			Object value = get(key);
			if ((value instanceof OperationMapImpl)) {
				map.put(key, ((OperationMapImpl) value).asMap());
			} else if ((value instanceof OperationListImpl)) {
				map.put(key, ((OperationListImpl) value).asList());
			} else {
				map.put(key, value);
			}
		}
		return Collections.unmodifiableMap(map);
	}
	@Override
	public void clear() {
		this.properties.clear();
	}

	@Override
	public void putAnyAll(Map<?, ?> map) {
		for(Object key:map.keySet())
			if(null != key)
				doPut(key.toString(),map.get(key));
	}

	@Override
	public InsightJsonObject toJson() throws JSONException {
		InsightJsonObject ret = new InsightJsonObject();
		toInsightJson(ret,this);
 		return ret;
	}
	private void toInsightJson(InsightJsonObject obj,OperationMap map) throws JSONException
	{
		for(String key:map.keySet())
		{
			Object value = map.get(key);
			if(value == null)
				continue;
			if(value instanceof OperationMap)
			{
				InsightJsonObject inside = new InsightJsonObject();
				toInsightJson(inside,(OperationMap)value);
				obj.put(key, inside);
			}
			else if(value instanceof OperationList )
			{
				List<Object> list =((OperationList)value).asList();
				obj.put(key, list);
			}
			else  
				obj.put(key, value.toString());
		}
	}
}