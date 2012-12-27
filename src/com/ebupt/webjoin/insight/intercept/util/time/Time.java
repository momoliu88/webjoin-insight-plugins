package com.ebupt.webjoin.insight.intercept.util.time;

import java.io.Serializable;
import java.util.Date;

public class Time implements Serializable, Comparable<Time> {
	private static final long serialVersionUID = 2493100846252835950L;
	private static final Time TIME_IN_NANOS_0 = new Time(0L);
	private static final Time TIME_IN_NANOS_1 = new Time(1L);
	private long nanos;

	protected Time() {
	}

	public Time(long nanos) {
		this.nanos = nanos;
	}

	public Time(Date date) {
		this.nanos = TimeUtil.millisToNanos(date.getTime());
	}

	public Date asDate() {
		return new Date(getMillis());
	}

	public long getNanos() {
		return this.nanos;
	}

	public long getMillis() {
		return TimeUtil.nanosToMillis(this.nanos);
	}

	public int getSeconds() {
		return TimeUtil.nanosToSeconds(this.nanos);
	}

	public Time plus(Time other) {
		return inNanos(other.nanos + this.nanos);
	}

	public Time minus(Time other) {
		return inNanos(this.nanos - other.nanos);
	}

	public boolean alignsOnGranularity(Granularity g) {
		return nanosAlignOnGranularity(this.nanos, g);
	}

	public static boolean nanosAlignOnGranularity(long nanos, Granularity g) {
		return nanos % g.getNanos() == 0L;
	}

	public Time alignDownToGranularity(Granularity g) {
		long granNanos = g.getNanos();
		if (this.nanos < 0L) {
			return inNanos((this.nanos - granNanos + 1L) / granNanos
					* granNanos);
		}
		return inNanos(this.nanos / granNanos * granNanos);
	}

	public Time alignUpToGranularity(Granularity g) {
		long granNanos = g.getNanos();
		if (this.nanos > 0L) {
			return inNanos((this.nanos + granNanos - 1L) / granNanos
					* granNanos);
		}
		return inNanos(this.nanos / granNanos * granNanos);
	}

	public String toString() {
		return Long.toString(this.nanos);
	}

	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (int) (this.nanos ^ this.nanos >>> 32);
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Time))
			return false;
		Time other = (Time) obj;
		if (this.nanos != other.nanos)
			return false;
		return true;
	}

	public static Time inNanos(long nanos) {
		if (nanos == 0L)
			return TIME_IN_NANOS_0;
		if (nanos == 1L) {
			return TIME_IN_NANOS_1;
		}
		return new Time(nanos);
	}

	public static Time inSeconds(int seconds) {
		return new Time(TimeUtil.secondsToNanos(seconds));
	}

	public static Time inMinutes(int minutes) {
		return new Time(TimeUtil.secondsToNanos(minutes * 60));
	}

	public static Time inMillis(long millis) {
		return new Time(TimeUtil.millisToNanos(millis));
	}

	public static Time inHours(int hours) {
		return new Time(TimeUtil.secondsToNanos(hours * 60 * 60));
	}

	public static Time inDays(int days) {
		return new Time(TimeUtil.secondsToNanos(days * 24 * 60 * 60));
	}

	public int compareTo(Time o) {
		if (this.nanos < o.nanos)
			return -1;
		if (this.nanos == o.nanos) {
			return 0;
		}
		return 1;
	}

	public boolean isBefore(Time other) {
		return compareTo(other) < 0;
	}

	public boolean isAfter(Time other) {
		return compareTo(other) > 0;
	}

	public long divideBy(Time gran) {
		return this.nanos / gran.getNanos();
	}

	public Time divideBy(long x) {
		return inNanos(this.nanos / x);
	}

	public Granularity asGranularity() {
		return Granularity.inNanos(this.nanos);
	}
}