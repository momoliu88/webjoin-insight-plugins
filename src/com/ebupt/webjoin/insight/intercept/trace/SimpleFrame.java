package com.ebupt.webjoin.insight.intercept.trace;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.util.time.Time;
import com.ebupt.webjoin.insight.intercept.util.time.TimeRange;

public class SimpleFrame implements Frame, Serializable {
	private static final long serialVersionUID = -1411614629234309053L;
	private FrameId id;
	private TimeRange range;
	private Time startTime;
	private Operation operation;
	private List<Frame> children;
	private Frame parent;

	@SuppressWarnings("unused")
	private SimpleFrame() {
	}

	public SimpleFrame(Frame frame) {
		this.id = frame.getId();
		this.parent = frame.getParent();
		this.operation = frame.getOperation();
		this.range = frame.getRange();
		this.children = Collections.unmodifiableList(frame.getChildren());
		this.startTime = frame.getStart();
	}

	public SimpleFrame(FrameId id, Frame parent, Operation op, TimeRange range,
			List<Frame> children) {
		this.id = id;
		this.parent = parent;
		this.operation = op;
		this.range = range;
		this.children = Collections.unmodifiableList(children);
		this.startTime = range.getStartTime();
	}

	public Operation getOperation() {
		return this.operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public List<Frame> getChildren() {
		return this.children;
	}

	public void setChildren(List<Frame> children) {
		this.children = Collections.unmodifiableList(children);
	}

	public TimeRange getRange() {
		return this.range;
	}

	public boolean isRoot() {
		return this.parent == null;
	}

	public FrameId getId() {
		return this.id;
	}

	public Frame getParent() {
		return this.parent;
	}

	public void setParent(Frame parent) {
		this.parent = parent;
	}

	@Override
	public Time getStart() {
		return this.startTime;
	}
}