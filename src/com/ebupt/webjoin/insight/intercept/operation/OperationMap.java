package com.ebupt.webjoin.insight.intercept.operation;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;

import com.ebupt.webjoin.insight.json.InsightJsonObject;

public abstract interface OperationMap {
	public abstract Object get(String paramString);

	public abstract <C> C get(String paramString, Class<C> paramClass);

	public abstract <C> C get(String paramString, Class<C> paramClass, C paramC);

	public abstract int size();

	public abstract Set<String> keySet();

	public abstract OperationMap put(String paramString, boolean paramBoolean);
	
	public abstract InsightJsonObject toJson()  throws JSONException;

	public abstract OperationMap put(String paramString, byte paramByte);

	public abstract OperationMap put(String paramString, char paramChar);

	public abstract OperationMap put(String paramString, Date paramDate);

	public abstract OperationMap put(String paramString, double paramDouble);

	public abstract OperationMap put(String paramString, float paramFloat);

	public abstract OperationMap put(String paramString, int paramInt);

	public abstract OperationMap put(String paramString, long paramLong);

	public abstract OperationMap put(String paramString, short paramShort);

	public abstract OperationMap put(String paramString1, String paramString2);

	public abstract OperationMap putAny(String paramString, Object paramObject);

	public abstract OperationMap putAnyNonEmpty(String paramString,
			Object paramObject);

	public abstract OperationMap createMap(String paramString);

	public abstract OperationList createList(String paramString);

	public abstract void clear();
	public abstract Map<String,Object> asMap();

	public abstract void putAnyAll(Map<?,?> asMap);
}