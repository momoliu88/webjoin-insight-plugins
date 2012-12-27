package com.ebupt.webjoin.insight.intercept.trace;

import java.util.List;
import java.util.Map;

public abstract interface FrameBuilderCallback
{
  public abstract List<FrameBuilderEvent> listensTo();

  public abstract void exitRootFrame(Frame paramFrame, Map<String, Object> paramMap);

  public abstract void enterRootFrame();

  public abstract void enterChildFrame(Frame paramFrame);

  public abstract void exitChildFrame(Frame paramFrame);

  public static enum FrameBuilderEvent
  {
    ROOT_ENTER, 
    ROOT_EXIT, 
    CHILD_ENTER, 
    CHILD_EXIT;
  }
}