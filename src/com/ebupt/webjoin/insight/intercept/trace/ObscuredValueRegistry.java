package com.ebupt.webjoin.insight.intercept.trace;

import java.util.Set;

public abstract interface ObscuredValueRegistry
{
  public abstract void markObscured(Object paramObject);

  public abstract Set<Object> getValues();
}