package com.ebupt.webjoin.insight.intercept.util.time;

import java.util.Comparator;

public class Granularity extends Time {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8988534499222174234L;

	private Granularity(Time duration) {
		super(duration.getNanos());
	}

	private Granularity() {
	}

	public Granularity times(int i) {
		return inNanos(getNanos() * i);
	}

	public boolean isBiggerThan(Granularity other) {
		return compareTo(other) > 0;
	}

	public boolean isSmallerThan(Granularity other) {
		return compareTo(other) < 0;
	}

	public static Granularity inNanos(long nanos) {
		return new Granularity(Time.inNanos(nanos));
	}

	public static Granularity inSeconds(int seconds) {
		return new Granularity(Time.inSeconds(seconds));
	}

	public static Granularity inMinutes(int minutes) {
		return new Granularity(Time.inMinutes(minutes));
	}

	public static Granularity inHours(int hours) {
		return new Granularity(Time.inHours(hours));
	}

	public static Granularity inDays(int days) {
		return new Granularity(Time.inDays(days));
	}

	public String toString() {
		if (getSeconds() < 1) {
			return getNanos() + " nanos";
		}
		return getSeconds() + " seconds";
	}

	public boolean isEvenMultipleOf(Granularity sub) {
		return getNanos() % sub.getNanos() == 0L;
	}

	public static class LengthDescendingComparator implements
			Comparator<Granularity> {
		public static final LengthDescendingComparator INSTANCE = new LengthDescendingComparator();

		public int compare(Granularity a, Granularity b) {
			return b.compareTo(a);
		}
	}

	public static class LengthAscendingComparator implements
			Comparator<Granularity> {
		public static final LengthAscendingComparator INSTANCE = new LengthAscendingComparator();

		public int compare(Granularity a, Granularity b) {
			return a.compareTo(b);
		}
	}
}