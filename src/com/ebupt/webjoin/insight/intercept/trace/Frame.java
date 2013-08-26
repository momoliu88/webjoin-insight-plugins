package com.ebupt.webjoin.insight.intercept.trace;

import java.util.List;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.util.time.Time;
import com.ebupt.webjoin.insight.intercept.util.time.TimeRange;

public interface Frame {
	public abstract TimeRange getRange();

	public abstract Time getStart();
	public abstract Operation getOperation();

	public abstract void setOperation(Operation paramOperation);

	public abstract List<Frame> getChildren();

	public abstract boolean isRoot();

	public abstract FrameId getId();

	public abstract Frame getParent();
}
