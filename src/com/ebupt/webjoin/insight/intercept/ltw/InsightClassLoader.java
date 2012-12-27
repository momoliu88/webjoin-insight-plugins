package com.ebupt.webjoin.insight.intercept.ltw;

import java.net.URL;
import java.util.Collection;

import com.ebupt.webjoin.insight.application.ApplicationName;

public abstract interface InsightClassLoader
{
  public abstract void addLookupUrl(URL paramURL);

  public abstract Collection<URL> getLookupUrls();

  public abstract ApplicationName getApplicationName();

  public abstract void setApplicationName(ApplicationName paramApplicationName);
}