package com.ebupt.webjoin.insight.intercept;

import java.util.List;

import com.ebupt.webjoin.insight.intercept.trace.Trace;

public abstract interface InterceptDispatcher
{
  public abstract void dispatchTrace(Trace paramTrace);

  public abstract void register(InterceptListener paramInterceptListener);

  public abstract boolean unregister(InterceptListener paramInterceptListener);

  public abstract List<InterceptListener> getListeners();
}