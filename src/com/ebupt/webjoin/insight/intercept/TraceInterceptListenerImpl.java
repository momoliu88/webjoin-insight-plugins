package com.ebupt.webjoin.insight.intercept;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.json.JSONException;

import com.ebupt.webjoin.insight.HttpClientSender;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.intercept.trace.TraceType;
import com.ebupt.webjoin.insight.json.InsightJsonArray;
import com.ebupt.webjoin.insight.json.InsightJsonObject;
import com.ebupt.webjoin.insight.util.ListUtil;

public class TraceInterceptListenerImpl implements TraceInterceptListener {

	private InsightJsonObject printChildProps(Frame frame) {
		InsightJsonObject obj = new InsightJsonObject();

		if (null == frame)
			return obj;

		InsightJsonArray arr = new InsightJsonArray();
		for (Frame child : frame.getChildren()) {
			InsightJsonObject childobj = printChildProps(child);
			arr.put(childobj);
		}
		try {
			obj.put("desc", frameDes(frame.getOperation()));
			obj.put("operation_signature", frame.getOperation().getLabel());
			obj.put("id", UUID.randomUUID().toString());
			obj.put("duration", frame.getRange().getDuration());
			obj.put("start_time", frame.getRange().getStartTime().getNanos());
			if (arr.length() > 0)
				obj.put("frames", arr);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return obj;
	}

	@SuppressWarnings("unchecked")
	public InsightJsonObject frameDescription(String key,
			Map<String, Object> map, InsightJsonArray arr) throws JSONException {
		InsightJsonObject obj = new InsightJsonObject();
		InsightJsonObject params = new InsightJsonObject();
		for (Entry<String, Object> entry : map.entrySet()) {
			if(entry.getValue() == null)
				continue;
			StringBuffer buffer = new StringBuffer();
			if (entry.getValue() instanceof Map) {
				InsightJsonObject asMap = frameDescription(entry.getKey(),
						(Map<String, Object>) entry.getValue(), arr);
				if(null != asMap)
					arr.put(asMap);
				continue;
			} 
			if (entry.getValue() instanceof List) {
				buffer.append(ListUtil.combine((List<Object>) entry.getValue(),
						','));
			} else{
					buffer.append(entry.getValue().toString());
			}
//				buffer.append(entry.getValue() == null ? "" : entry.getValue()
//						.toString());
		//	if(entry.getValue()!=null)
			if(buffer.length() > 0)
				params.put(entry.getKey(), buffer.toString());

		}
		if(params.length() > 0 )
		{
			obj.put("params", params);
			obj.put("title", key);
			return obj;
		}
		return null;
	}

	public InsightJsonArray frameDes(Operation op) throws JSONException {
		Map<String, Object> map = op.asMap();
		System.out.println("op.map "+map);
		InsightJsonArray arr = new InsightJsonArray();
		InsightJsonObject obj = frameDescription("properties", map, arr);
		if(obj != null)
			arr.put(obj);
		return arr;
	}

	@Override
	public void handleTraceDispatch(Trace trace) {

		InsightJsonObject jsonObj = new InsightJsonObject();
		try {
			jsonObj.put("pid", trace.getPid());
			TraceType type = trace.getType() == null ? TraceType.LIFECYLE
					: trace.getType();
			jsonObj.put("trace_group", type.name());
			jsonObj.put("user_id", trace.getUserId());
			jsonObj.put("trace_id", trace.getId().toString());
			jsonObj.put("trace_start_time", trace.getRootFrame().getRange()
					.getStartTime().getMillis());
			jsonObj.put("trace_start_time_ns", trace.getRootFrame().getRange()
					.getStartTime().getNanos());
			jsonObj.put("trace_duration", trace.getRootFrame().getRange()
					.getDuration());
			jsonObj.put("target_application", trace.getAppName().toString());
			jsonObj.put("endpoint", trace.getRootFrame().getOperation()
					.getLabel());
			jsonObj.put("frames", new InsightJsonArray()
					.put(printChildProps(trace.getRootFrame())));
			InsightJsonObject extra = trace.getExtraInfo();
			if (null != extra) {
				@SuppressWarnings("unchecked")
				Iterator<String> itr = extra.keys();
				while (itr.hasNext()) {
					String key = itr.next();
					jsonObj.put(key, extra.get(key));
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		System.out.println("send trace:"+jsonObj.toString());
		HttpClientSender sender = new HttpClientSender();
		sender.post("trace", jsonObj);
		sender.closeConn();
	}

}
