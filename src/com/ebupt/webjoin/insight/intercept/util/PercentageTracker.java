package com.ebupt.webjoin.insight.intercept.util;

import java.util.concurrent.atomic.AtomicInteger;

public class PercentageTracker {
	private final AtomicInteger totalValues = new AtomicInteger(0);
	private final AtomicInteger processedValues = new AtomicInteger(0);
	private volatile int ratio;
	private final String name;

	public PercentageTracker(String id, int threshold) {
		this.name = id;
		this.ratio = threshold;
	}

	public PercentageTracker(String id) {
		this(id, 100);
	}

	public final String getName() {
		return this.name;
	}

	public int getTrackedRatio() {
		return this.ratio;
	}

	public void setTrackedRatio(int threshold) {
		this.ratio = threshold;
	}

	public int getTotalValues() {
		return this.totalValues.get();
	}

	public int getProcessedValues() {
		return this.processedValues.get();
	}

	void setTotalValues(int totalCount) {
		this.totalValues.set(totalCount);
	}

	void setProcessedValues(int numProcessed) {
		this.processedValues.set(numProcessed);
	}

	public void reset() {
		this.totalValues.set(0);
		this.processedValues.set(0);
	}

	public int getCurrentRatio() {
		int threshold = getTrackedRatio();
		if (threshold <= 0) {
			return 0;
		}

		if (threshold >= 100) {
			return 100;
		}

		int totalCount = getTotalValues();
		int numProcessed = getProcessedValues();
		if ((totalCount <= 0) || (numProcessed <= 0)) {
			return 0;
		}

		return (int) (numProcessed * 100L / totalCount);
	}

	public boolean isValueProcessingAllowed() {
		int threshold = getTrackedRatio();
		if (threshold <= 0) {
			return false;
		}

		if (threshold >= 100) {
			return true;
		}

		int totalCount = this.totalValues.incrementAndGet();
		int numProcessed = this.processedValues.get();

		if ((totalCount <= 0) || (numProcessed < 0)
				|| (numProcessed > totalCount)) {
			return restart();
		}

		int percentage = (int) (numProcessed * 100L / totalCount);

		if (percentage >= threshold) {
			return false;
		}

		if ((numProcessed = this.processedValues.incrementAndGet()) < 0) {
			this.processedValues.set(0);
		}

		return true;
	}

	private boolean restart() {
		this.totalValues.set(1);
		this.processedValues.set(0);

		return true;
	}

	public String toString() {
		return getName() + "[processed=" + getProcessedValues() + ";total="
				+ getTotalValues() + ";tracked=" + getTrackedRatio()
				+ ";ratio=" + getCurrentRatio() + "]";
	}
}