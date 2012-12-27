package com.ebupt.webjoin.insight.intercept.trace;
/**
 * Marks a value to be obscured
 */
public abstract interface ObscuredValueMarker {
	public abstract void markObscured(Object paramObject);
}