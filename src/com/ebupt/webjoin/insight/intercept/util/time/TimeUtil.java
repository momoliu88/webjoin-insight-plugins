package com.ebupt.webjoin.insight.intercept.util.time;

import java.util.Date;

public abstract class TimeUtil {
	public static final Date EPOCH = new Date(0L);
//3600*1000/rate
	public static long ratePerHourToMillisInterval(double rate) {
		if (rate <= 0.0D)
			return 0L;
		return ratePerMinuteToMillisInterval(rate / 60.0D);
	}

	public static long ratePerMinuteToMillisInterval(double rate) {
		if (rate <= 0.0D)
			return 0L;
		return ratePerSecondToMillisInterval(rate / 60.0D);
	}

	public static long ratePerSecondToMillisInterval(double rate) {
		if (rate <= 0.0D)
			return 0L;
		return (long) (1000.0D / rate);
	}

	public static long nanosToMillis(long nanos) {
		return nanos / 1000000L;
	}

	public static double nanosToFractionalMillis(long nanos) {
		return nanos / 1000000.0D;
	}

	public static int nanosToSeconds(long nanos) {
		return (int) (nanos / 1000000000L);
	}

	public static double nanosToFractionalMinutes(long nanos) {
		return nanos / 60000000000.0D;
	}

	public static double nanosToFractionalHours(long nanos) {
		return nanos / 3600000000000.0D;
	}

	public static long millisToNanos(long millis) {
		return millis * 1000000L;
	}

	public static int millisToSeconds(long millis) {
		return (int) (millis / 1000L);
	}

	public static int millisToMinutes(long millis) {
		return millisToSeconds(millis) / 60;
	}

	public static long secondsToNanos(int seconds) {
		return seconds * 1000000000L;
	}

	public static long secondsToMillis(int seconds) {
		return seconds * 1000L;
	}

	public static long nanosToMinutes(long duration) {
		return duration / 60000000000L;
	}

	public static long fractionalMillisToNanos(double millis) {
		return (long) (millis * 1000000.0D);
	}

	public static long daysToMillis(int days) {
		return days * 24L * 60L * 60L * 1000L;
	}

	public static long minutesToMillis(int minutes) {
		return minutes * 60L * 1000L;
	}

	public static boolean sleep(Time wait) {
		try {
			Thread.sleep(wait.getMillis());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		return true;
	}
}