package com.ebupt.webjoin.insight.json;

import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

public class InsightJsonArray extends JSONArray {
	public InsightJsonArray()
	{
		super();
	}
	public InsightJsonArray(@SuppressWarnings("rawtypes") Collection collection)
	{
		super(collection);
	}
	public InsightJsonArray(Object array) throws JSONException
	{
		super(array);
	}
	public InsightJsonArray(JSONTokener x) throws JSONException
	{
		super(x);
	}
	public InsightJsonArray(String source) throws JSONException
	{
		super(source);
	}
}
