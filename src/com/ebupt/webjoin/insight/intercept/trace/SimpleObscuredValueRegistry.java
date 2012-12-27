package com.ebupt.webjoin.insight.intercept.trace;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimpleObscuredValueRegistry implements ObscuredValueRegistry {
	private ConcurrentMap<Object, Object> sensitiveValues = new ConcurrentHashMap<Object, Object>();

	public void markObscured(Object o) {
		if (o == null) {
			return;
		}
		this.sensitiveValues.put(o, o);
	}

	public Set<Object> getValues() {
		return Collections.unmodifiableSet(this.sensitiveValues.keySet());
	}
}