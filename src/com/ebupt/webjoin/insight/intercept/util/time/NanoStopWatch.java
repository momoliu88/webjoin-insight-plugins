package com.ebupt.webjoin.insight.intercept.util.time;

public class NanoStopWatch implements StopWatch {
	private long startNanoOffset;
	private long startNanos;
	private boolean isStarted;
	private long lastMark;

	public NanoStopWatch() {
		this.isStarted = false;
	}

	public void start(long startNanos) {
		if (this.isStarted) {
			throw new IllegalStateException(
					"start() called, but already running");
		}
		this.isStarted = true;

		this.startNanoOffset = System.nanoTime();
		this.startNanos = startNanos;
		this.lastMark = startNanos;
	}

	public long mark() {
		if (!this.isStarted) {
			throw new IllegalStateException("mark() called, but not started");
		}
		long endNanoOffset = System.nanoTime();
		long duration = endNanoOffset - this.startNanoOffset;
		assert (duration >= 0L);

		long endNanos = this.startNanos + duration;

		if (endNanos <= this.lastMark) {
			endNanos = this.lastMark + 1L;
		}
		this.lastMark = endNanos;
		return this.lastMark;
	}

	public static class NanoStopWatchFactory implements StopWatchFactory {
		public StopWatch createWatch() {
			return new NanoStopWatch();
		}
	}
}