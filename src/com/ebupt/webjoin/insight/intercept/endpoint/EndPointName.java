package com.ebupt.webjoin.insight.intercept.endpoint;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.resource.ResourceName;

public class EndPointName
  implements ResourceName, Serializable
{
  private static final long serialVersionUID = 8195550555176520352L;
  private static final Map<String, EndPointName> endpointsMap = Collections.synchronizedMap(new WeakHashMap<String, EndPointName>());
  private String name;

  private EndPointName()
  {
  }

  private EndPointName(String name)
  {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public static EndPointName valueOf(String name) {
    EndPointName endPoint = endpointsMap.get(name);
    if (endPoint == null) {
      endPoint = new EndPointName(name);
      endpointsMap.put(name, endPoint);
    }
    return endPoint;
  }

  public static EndPointName valueOf(Operation op)
  {
    return valueOf(op.get("className") + "#" + op.get("methodSignature"));
  }

  public String toString()
  {
    return this.name;
  }

  public int hashCode()
  {
    return this.name.hashCode();
  }

  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    EndPointName other = (EndPointName)obj;
    return this.name.equals(other.name);
  }
/*
  public ResourceKey makeKey() {
    return ResourceKey.valueOf("EndPoint", this.name);
  }*/
}