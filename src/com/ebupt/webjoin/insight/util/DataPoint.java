package com.ebupt.webjoin.insight.util;

import java.io.Serializable;

import com.ebupt.webjoin.insight.intercept.util.time.Time;
import com.ebupt.webjoin.insight.intercept.util.time.TimeRange;
import com.ebupt.webjoin.insight.intercept.util.time.TimeUtil;

public class DataPoint implements IDataPoint, Serializable {
	private static final long serialVersionUID = -890421277280635958L;
	//start time in seconds
	private int time;
	//duration in Ms
	private double value;
	private volatile transient Time timestamp;

	@SuppressWarnings("unused")
	private DataPoint() {
	}

	public DataPoint(int time, double value) {
		this.time = time;
		this.value = value;
	}

	public DataPoint(Time timestamp, double value) {
		this.timestamp = timestamp;
		this.time = timestamp.getSeconds();
		this.value = value;
	}

	public int getTime() {
		return this.time;
	}

	public Time getTimeStamp() {
		if (this.timestamp == null) {
			this.timestamp = Time.inSeconds(this.time);
		}
		return this.timestamp;
	}

	public long getTimeNanos() {
		return TimeUtil.secondsToNanos(this.time);
	}

	public double getValue() {
		return this.value;
	}

	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = 31 * result + this.time;

		long temp = Double.doubleToLongBits(this.value);
		result = prime * result + (int) (temp ^ temp >>> 32);
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Serializable)) {
			return false;
		}
		IDataPoint other = (IDataPoint) obj;
		if (getTime() != other.getTime())
			return false;
		if (Double.doubleToLongBits(getValue()) != Double
				.doubleToLongBits(other.getValue()))
			return false;
		return true;
	}

	public String toString() {
		return "(" + this.time + ", " + this.value + ")";
	}

	public static DataPoint dp(int time, double value) {
		return new DataPoint(time, value);
	}

	public static DataPoint dp(Time time, double value) {
		return new DataPoint(time, value);
	}

	public static DataPoint dataPointFromRange(TimeRange range) {
		return new DataPoint(TimeUtil.nanosToSeconds(range.getStart()),
				range.getDurationMillis());
	}
}