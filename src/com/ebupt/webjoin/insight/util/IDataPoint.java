package com.ebupt.webjoin.insight.util;

import java.util.Comparator;

import com.ebupt.webjoin.insight.intercept.util.time.Time;

public interface IDataPoint {
	public static final Comparator<IDataPoint> ASCENDING_DATE_COMPARATOR = new Comparator<IDataPoint>() {
		public int compare(IDataPoint pt1, IDataPoint pt2) {
			return pt1.getTime() - pt2.getTime();
		}
	};

	public int getTime();

	public Time getTimeStamp();

	public double getValue();

	public int hashCode();

	public boolean equals(Object paramObject);
}