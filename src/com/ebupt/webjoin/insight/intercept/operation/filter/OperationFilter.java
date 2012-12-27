package com.ebupt.webjoin.insight.intercept.operation.filter;

 
import java.util.Set;

import com.ebupt.webjoin.insight.intercept.trace.FrameId;
import com.ebupt.webjoin.insight.intercept.trace.Trace;

public abstract interface OperationFilter
{
  public abstract String getFilterLabel();

  public abstract int matchingFrames(Trace paramTrace);

  public abstract Set<FrameId> includeFrames(Trace paramTrace);

  public abstract Set<FrameId> excludeFrames(Trace paramTrace);

  public abstract boolean isInversable();
}