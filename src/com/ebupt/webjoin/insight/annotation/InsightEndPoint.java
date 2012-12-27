package com.ebupt.webjoin.insight.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.METHOD})
public @interface InsightEndPoint
{
  public abstract String comment();

  public abstract String label();

  public abstract String example();

  public abstract int score();
}