package com.ebupt.webjoin.insight.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.METHOD})
public @interface InsightOperation
{
  public abstract String comment();

  public abstract String label();
}