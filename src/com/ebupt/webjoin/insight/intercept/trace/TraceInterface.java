package com.ebupt.webjoin.insight.intercept.trace;

import java.util.Date;

import com.ebupt.webjoin.insight.application.ApplicationName;

public abstract interface TraceInterface
{
  public abstract ApplicationName getAppName();

  public abstract TraceId getId();

  public abstract Date getDate();
}