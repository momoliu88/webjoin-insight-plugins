package com.ebupt.webjoin.insight;

import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.intercept.ltw.ClassLoaderUtils;
import com.ebupt.webjoin.insight.intercept.ltw.InsightClassLoader;

 

public class InsightAgentClassloadingHelper extends InsightAgentPluginsHelper
{
  private static final InsightAgentClassloadingHelper instance = new InsightAgentClassloadingHelper();

  private ThreadLocal<ApplicationName> appNames = new ThreadLocal<ApplicationName>();
  public static final String IGNORE_MISSING_APP_PROP = "insight.bootstrap.ignore.missing.appname";

  public static InsightAgentClassloadingHelper getInstance()
  {
    return instance;
  }

  public ApplicationName rememberApplicationName(ApplicationName appName)
  {
    ApplicationName prev = (ApplicationName)this.appNames.get();
    if (appName == null)
      this.appNames.remove();
    else {
      this.appNames.set(appName);
    }
    return prev;
  }

  public ApplicationName setLoaderApplication(InsightClassLoader loader)
    throws IllegalStateException
  {
    ApplicationName appName = retrieveApplicationName(false);
    if (appName == null) {
      throw new IllegalStateException("No application name bound for " + loader.getClass().getName() + ": " + loader);
    }

    loader.setApplicationName(appName);
    return appName;
  }

  public ApplicationName retrieveApplicationName(boolean ensureExists)
    throws IllegalStateException
  {
    return retrieveApplicationName(ensureExists, true);
  }

  ApplicationName retrieveApplicationName(boolean ensureExists, boolean useDefault)
    throws IllegalStateException
  {
    ApplicationName name = (ApplicationName)this.appNames.get();

    if ((name == null) && (useDefault)) {
      String ignoreVal = System.getProperty("insight.bootstrap.ignore.missing.appname");
      if ((ignoreVal != null) && (ignoreVal.length() > 0)) {
        name = ApplicationName.valueOf(ignoreVal);
      }

    }

    if ((name != null) || (!ensureExists)) {
      return name;
    }

    throw new IllegalStateException("Application name not bound! Probably a bug, please check your code");
  }

  public ApplicationName clearApplicationName()
  {
    ApplicationName prev = (ApplicationName)this.appNames.get();
    this.appNames.remove();
    return prev;
  }

  public InsightClassLoader resolveLoaderApplicationName(InsightClassLoader loader)
    throws IllegalStateException
  {
    InsightClassLoader insightLoader = null;
    ApplicationName appName = retrieveApplicationName(false);

    if (appName != null) {
      insightLoader = loader;
    }
    else {
      if ((loader instanceof ClassLoader)) {
        insightLoader = ClassLoaderUtils.findInsightWeavingClassLoader(((ClassLoader)loader).getParent());
      }

      if (insightLoader == null) {
        Thread curThread = Thread.currentThread();
        insightLoader = ClassLoaderUtils.findInsightWeavingClassLoader(curThread.getContextClassLoader());
      }

      if (insightLoader == null) {
        throw new IllegalStateException("No InsightClassLoader in hierarchy of " + loader.getClass().getName() + ": " + loader);
      }

      if ((appName = insightLoader.getApplicationName()) == null) {
        throw new IllegalStateException("No application name set for InsightClassLoader " + insightLoader + " in hierarchy of " + loader.getClass().getName() + ": " + loader);
      }
    }

    loader.setApplicationName(appName);
    return insightLoader;
  }
}