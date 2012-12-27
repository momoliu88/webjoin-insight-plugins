package com.ebupt.webjoin.insight.intercept.operation;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public abstract interface OperationList
{
  public abstract Object get(int paramInt);

  public abstract <C> C get(int paramInt, Class<C> paramClass);

  public abstract int size();

  public abstract OperationList add(boolean paramBoolean);

  public abstract OperationList add(byte paramByte);

  public abstract OperationList add(char paramChar);

  public abstract OperationList add(Date paramDate);

  public abstract OperationList add(double paramDouble);

  public abstract OperationList add(float paramFloat);

  public abstract OperationList add(int paramInt);

  public abstract OperationList add(long paramLong);

  public abstract OperationList add(short paramShort);

  public abstract OperationList add(String paramString);

  public abstract OperationList addAny(Object paramObject);

  public abstract OperationList addAnyNonEmpty(Object paramObject);

  public abstract OperationList addAll(Collection<?> paramCollection);

  public abstract OperationList update(int paramInt, Object paramObject);

  public abstract OperationMap createMap();

  public abstract List<Object> asList();
  
  public abstract OperationList createList();

  public abstract OperationList shallowCopy();

  public abstract void clear();
}