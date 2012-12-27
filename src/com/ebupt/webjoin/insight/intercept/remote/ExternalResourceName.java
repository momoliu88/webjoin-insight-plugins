package com.ebupt.webjoin.insight.intercept.remote;


import java.io.Serializable;

import com.ebupt.webjoin.insight.resource.ResourceKey;
import com.ebupt.webjoin.insight.resource.ResourceName;

public class ExternalResourceName
  implements ResourceName, Serializable
{
  private static final long serialVersionUID = 8195550555176520352L;
  private static final String DEFAULT_LABEL = "Default";
  private String name;

  private ExternalResourceName()
  {
  }

  private ExternalResourceName(String name)
  {
    this.name = name;
  }

  public String getName() {
    return this.name == null ? DEFAULT_LABEL : this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public static ExternalResourceName valueOf(String name) {
    return new ExternalResourceName(name);
  }

  public String toString()
  {
    return getName();
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
    ExternalResourceName other = (ExternalResourceName)obj;
    return this.name.equals(other.name);
  }

  public ResourceKey makeKey() {
    return ResourceKey.valueOf("External", this.name);
  }
}