package com.ebupt.webjoin.insight.intercept;

public abstract interface LazyConstructor
{
  public abstract boolean isFinalizable();

  public abstract void finalizeConstruction();
}