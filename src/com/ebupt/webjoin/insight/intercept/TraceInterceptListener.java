package com.ebupt.webjoin.insight.intercept;

import com.ebupt.webjoin.insight.intercept.trace.Trace;


public interface TraceInterceptListener extends InterceptListener
{
  public void handleTraceDispatch(Trace paramTrace);
}