package com.ebupt.webjoin.insight.intercept.util.time;

import java.util.Date;

public class ImmutableDate extends Date {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8630200416587234982L;

	public ImmutableDate() {
	}

	public ImmutableDate(long date) {
		super(date);
	}

	public ImmutableDate(Date date) {
		this(date.getTime());
	}

	public void setYear(int year) {
		throw new UnsupportedOperationException("setYear(" + year + ") N/A");
	}

	public void setMonth(int month) {
		throw new UnsupportedOperationException("setMonth(" + month + ") N/A");
	}

	public void setDate(int date) {
		throw new UnsupportedOperationException("setDate(" + date + ") N/A");
	}

	public void setHours(int hours) {
		throw new UnsupportedOperationException("setHours(" + hours + ") N/A");
	}

	public void setMinutes(int minutes) {
		throw new UnsupportedOperationException("setMinutes(" + minutes
				+ ") N/A");
	}

	public void setSeconds(int seconds) {
		throw new UnsupportedOperationException("setSeconds(" + seconds
				+ ") N/A");
	}

	public void setTime(long time) {
		throw new UnsupportedOperationException("setTime(" + time + ") N/A");
	}
}