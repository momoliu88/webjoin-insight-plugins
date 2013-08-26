package com.ebupt.webjoin.insight.intercept.trace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.util.time.StopWatch;
import com.ebupt.webjoin.insight.intercept.util.time.Time;
import com.ebupt.webjoin.insight.intercept.util.time.TimeRange;

public class StopWatchFrame implements Frame, Serializable {
	private static final long serialVersionUID = 3069437305929160221L;
	private FrameId id;
	private List<Frame> children;
	private Frame parent;
	private Operation operation;
	private TimeRange range;
	private transient long frameStart;
	private transient boolean entered;
	private transient boolean exited;
	private transient StopWatch watch;

	@SuppressWarnings("unused")
	private StopWatchFrame() {
	}

	public StopWatchFrame(FrameId id, Frame parent, StopWatch watch) {
		this.id = id;
		this.parent = parent;
		this.children = new ArrayList<Frame>();
		this.watch = watch;
	}
	@Override
	public Time getStart(){
		return new Time(this.frameStart);
	}
	
	public TimeRange getRange() {
		return this.range;
	}

	public void enter(Operation op) {
		if (this.entered) {
			throw new IllegalStateException("Cannot enter > 1 times");
		}
		this.entered = true;
		this.frameStart = this.watch.mark();
		setOperation(op);
	}

	public void exit() {
		if ((!this.entered) || (this.exited)) {
			throw new IllegalStateException("Not entered or already exited");
		}
		this.exited = true;
		this.range = new TimeRange(this.frameStart, this.watch.mark());
	}

	public List<Frame> getChildren() {
		return this.children;
	}

	public void addChild(Frame child) {
		this.children.add(child);
	}

	public Frame getParent() {
		return this.parent;
	}

	public Operation getOperation() {
		return this.operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public boolean isRoot() {
		return getParent() == null;
	}

	public FrameId getId() {
		return this.id;
	}

	public void setChildren(List<Frame> children) {
		this.children = children;
	}

	public void setRange(TimeRange range) {
		this.range = range;
	}

	public String toString() {
		return toString(this, 0);
	}

	public static String toString(Frame frame, int depth) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			sb.append("  ");
		}
		sb.append(frame.getOperation()).append(" duration:")
				.append(frame.getRange().getDuration()).append("\n");

		for (Frame child : frame.getChildren()) {
			sb.append(toString(child, depth + 1));
		}
		return sb.toString();
	}

	public void discard(StopWatchFrame frame) {
		this.children.remove(frame);
	}

	
}