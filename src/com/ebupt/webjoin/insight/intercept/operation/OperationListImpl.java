package com.ebupt.webjoin.insight.intercept.operation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

final class OperationListImpl implements OperationList, Serializable {
	private static final long serialVersionUID = -7220509902954488936L;
	private List<Object> items;

	OperationListImpl() {
		this.items = new ArrayList<Object>();
	}

	private OperationListImpl(OperationListImpl exist) {
		this.items = new ArrayList<Object>(exist.items);
	}

	public Object get(int index) {
		return this.items.get(index);
	}

	public <C> C get(int index, Class<C> clazz) {
		return get(index, clazz, null);
	}

	@SuppressWarnings("unchecked")
	public <C> C get(int index, Class<C> clazz, C defaultValue) {
		Object item = get(index);
		if ((item != null) && (clazz.isAssignableFrom(item.getClass()))) {
			return (C) item;
		}
		return defaultValue;
	}

	public int size() {
		return this.items.size();
	}

	private OperationListImpl doAdd(Object value) {
		this.items.add(value);
		return this;
	}

	public OperationList addAny(Object value) {
		return doAdd(OperationUtils.resolveOperationObject(value));
	}

	public OperationList addAnyNonEmpty(Object value) {
		Object effectiveValue = value == null ? null : OperationUtils
				.resolveOperationObject(value);
		if (effectiveValue == null) {
			return this;
		}

		if (((effectiveValue instanceof String))
				&& (((String) effectiveValue).length() <= 0)) {
			return this;
		}

		return doAdd(effectiveValue);
	}

	public OperationListImpl add(boolean value) {
		return doAdd(Boolean.valueOf(value));
	}

	public OperationListImpl add(byte value) {
		return doAdd(Byte.valueOf(value));
	}

	public OperationListImpl add(char value) {
		return doAdd(Character.valueOf(value));
	}

	public OperationListImpl add(Date value) {
		return doAdd(new Date(value.getTime()));
	}

	public OperationListImpl add(double value) {
		return doAdd(Double.valueOf(value));
	}

	public OperationListImpl add(float value) {
		return doAdd(Float.valueOf(value));
	}

	public OperationListImpl add(int value) {
		return doAdd(Integer.valueOf(value));
	}

	public OperationListImpl add(long value) {
		return doAdd(Long.valueOf(value));
	}

	public OperationListImpl add(short value) {
		return doAdd(Short.valueOf(value));
	}

	public OperationListImpl add(String value) {
		return doAdd(value);
	}

	public OperationListImpl addAll(Collection<?> value) {
		for (Iterator<?> itr = value.iterator(); itr.hasNext();) {
			Object x = itr.next();
			doAdd(x);
		}
		return this;
	}

	public OperationList update(int index, Object o) {
		this.items.set(index, o);
		return this;
	}

	public OperationMap createMap() {
		OperationMap map = new OperationMapImpl();
		doAdd(map);
		return map;
	}

	public OperationList createList() {
		OperationList list = new OperationListImpl();
		doAdd(list);
		return list;
	}

	public List<Object> asList() {
		List<Object> list = new ArrayList<Object>();
		for (Iterator<Object> itr = this.items.iterator(); itr.hasNext();) {
			Object item = itr.next();
			if ((item instanceof OperationMapImpl)) {
				list.add(((OperationMapImpl) item).asMap());
			} else if ((item instanceof OperationListImpl)) {
				list.add(((OperationListImpl) item).asList());
			} else {
				list.add(item);
			}
		}
		return Collections.unmodifiableList(list);
	}

	public OperationList shallowCopy() {
		return new OperationListImpl(this);
	}

	public void clear() {
		this.items.clear();
	}

	 
}