package com.ebupt.webjoin.insight.intercept.util;

public interface GroupingClosure<K, V>
{
  public K getGroupFor(V paramV);
}