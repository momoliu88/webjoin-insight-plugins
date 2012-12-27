package com.ebupt.webjoin.insight.intercept.util.time;

public abstract interface StopWatch {
	public abstract void start(long paramLong);

	public abstract long mark();
}