package com.ebupt.webjoin.insight.collection;

import com.ebupt.webjoin.insight.intercept.operation.Operation;


public abstract interface OperationCollector
{
  public abstract void enter(Operation paramOperation);

  public abstract void exitNormal();

  public abstract void exitNormal(Object paramObject);

  public abstract void exitAbnormal(Throwable paramThrowable);

  public abstract void exitAndDiscard();

  public abstract void exitAndDiscard(Object paramObject);
  

}