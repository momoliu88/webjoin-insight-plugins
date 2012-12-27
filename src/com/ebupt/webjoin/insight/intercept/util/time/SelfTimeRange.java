package com.ebupt.webjoin.insight.intercept.util.time;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SelfTimeRange extends TimeRange {
	private static final long serialVersionUID = -3775679286700566092L;
	private List<TimeRange> selfTimeFragments;
	private long selfTime;

	public SelfTimeRange() {
	}

	public SelfTimeRange(TimeRange elapsedTimeRange,
			List<TimeRange> childTimeRanges) {
		super(elapsedTimeRange.getStart(), elapsedTimeRange.getEnd());
		this.selfTimeFragments = new ArrayList<TimeRange>();
		this.selfTimeFragments.add(elapsedTimeRange);
		for (TimeRange childTimeRange : childTimeRanges) {
			subtractChildTimeRange(childTimeRange);
		}
		this.selfTimeFragments = Collections
				.unmodifiableList(this.selfTimeFragments);
		this.selfTime = timeInSelf(this.selfTimeFragments);
	}

	private void subtractChildTimeRange(TimeRange childTimeRange) {
		if (this.selfTimeFragments.size() <= 0) {
			return;
		}
		TimeRange endTimeRange = (TimeRange) this.selfTimeFragments
				.remove(this.selfTimeFragments.size() - 1);
		if (!endTimeRange.contains(childTimeRange)) {
			throw new IllegalStateException("Invalid time range: "
					+ endTimeRange + " child: " + childTimeRange);
		}
		long endTimeStart = endTimeRange.getStart();
		long nextStart = childTimeRange.getStart();
		long endTimeEnd = endTimeRange.getEnd();
		long childEnd = childTimeRange.getEnd();
		if ((endTimeStart >= nextStart) || (childEnd >= endTimeEnd)) {
			return;
		}
		this.selfTimeFragments.add(new TimeRange(endTimeStart, nextStart));
		this.selfTimeFragments.add(new TimeRange(childEnd, endTimeEnd));
	}

	private static long timeInSelf(
			Collection<? extends TimeRange> selfTimeFragments) {
		long selfTime = 0L;
		for (TimeRange selfFragment : selfTimeFragments) {
			selfTime += selfFragment.getDuration();
		}
		return selfTime;
	}

	public long getSelfDuration() {
		return this.selfTime;
	}

	public long getSelfDurationMillis() {
		return TimeUtil.nanosToMillis(this.selfTime);
	}

	public List<TimeRange> getSelfFragmentTimeRanges() {
		return this.selfTimeFragments;
	}

	public int hashCode() {
		int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ (this.selfTimeFragments == null ? 0 : this.selfTimeFragments
						.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SelfTimeRange other = (SelfTimeRange) obj;
		if (this.selfTimeFragments == null) {
			if (other.selfTimeFragments != null)
				return false;
		} else if (!this.selfTimeFragments.equals(other.selfTimeFragments))
			return false;
		return true;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(serialVersionUID);
		super.writeExternal(out);

		out.writeInt(this.selfTimeFragments.size());
		for (TimeRange range : this.selfTimeFragments) {
			out.writeObject(range);
		}
		out.writeLong(this.selfTime);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		long version = in.readLong();
		if (version != serialVersionUID) {
			throw new IllegalStateException(
					"Unable to deserialize this SelfTimeRange; unexpected serialVersionUID");
		}
		super.readExternal(in);
		int size = in.readInt();
		this.selfTimeFragments = new ArrayList<TimeRange>(size);
		while (size-- > 0) {
			this.selfTimeFragments.add((TimeRange) in.readObject());
		}
		this.selfTime = in.readLong();
	}
}