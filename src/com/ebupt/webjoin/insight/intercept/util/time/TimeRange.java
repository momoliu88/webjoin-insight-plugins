package com.ebupt.webjoin.insight.intercept.util.time;

import com.ebupt.webjoin.insight.intercept.util.GroupingClosure;
import com.ebupt.webjoin.insight.util.ListUtil;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TimeRange implements Externalizable {
	private static final long serialVersionUID = -1425636893547616276L;
	public static final TimeRange FULL_RANGE = nanoTimeRange(1L,
			9223372036854775806L);
	private long start; //nanos
	private long end; //nanos
	private volatile transient Time startTime;
	private volatile transient Time endTime;
	private volatile transient Date startDate;
	private volatile transient Date endDate;
	private volatile transient Granularity durationGranularity;

	public TimeRange() {
	}

	public TimeRange(TimeRange orig) {
		this(orig.getStart(), orig.getEnd());
	}

	public TimeRange(long start, long end) {
		if ((start < 0L) || (end <= start)) {
			throw new IllegalArgumentException(
					"End time must be > start time, and >= 0.  start=" + start
							+ " end=" + end);
		}
		this.start = start;
		this.end = end;
	}

	public TimeRange(Date start, Date end) {
		this(TimeUtil.millisToNanos(start.getTime()), TimeUtil
				.millisToNanos(end.getTime()));
	}

	public static TimeRange makeWindowedTimeRangeMillis(Date currentDate,
			long duration, int windows) {
		long windowPeriod = duration / windows;
		long endRaw = currentDate.getTime();
		long windowOffset = endRaw % windowPeriod;
		long end = endRaw + (windowPeriod - windowOffset);
		long start = end - duration;
		return new TimeRange(TimeUtil.millisToNanos(start),
				TimeUtil.millisToNanos(end));
	}

	public boolean after(long nanos) {
		return nanos < this.start;
	}

	public boolean after(Date date) {
		return after(TimeUtil.millisToNanos(date.getTime()));
	}

	public boolean after(Time time) {
		return after(time.getNanos());
	}

	public boolean before(long nanos) {
		return nanos > this.end;
	}

	public boolean before(Date date) {
		return before(TimeUtil.millisToNanos(date.getTime()));
	}

	public boolean before(Time time) {
		return before(time.getNanos());
	}

	public boolean contains(long nanos) {
		return (nanos >= this.start) && (nanos < this.end);
	}

	public boolean contains(Date date) {
		return contains(TimeUtil.millisToNanos(date.getTime()));
	}

	public boolean contains(Time time) {
		return contains(time.getNanos());
	}

	public boolean contains(TimeRange sub) {
		return (contains(sub.getStart())) && (contains(sub.getEnd()));
	}

	public boolean containsOrAlignsWith(TimeRange sub) {
		return (contains(sub.getStart())) && (this.end >= sub.getEnd());
	}

	public boolean overlaps(TimeRange other) {
		if ((other.contains(this)) || (contains(other))) {
			return true;
		}

		if ((contains(other.getStart())) || (contains(other.getEnd() - 1L))) {
			return true;
		}
		return false;
	}

	public long getStart() {
		return this.start;
	}

	public TimeRange intersect(TimeRange r) {
		if ((this.start <= r.start) && (this.end > r.start)
				&& (this.end <= r.end)) {
			return new TimeRange(r.start, this.end);
		}

		if ((r.start <= this.start) && (r.end > this.start)
				&& (r.end <= this.end)) {
			return new TimeRange(this.start, r.end);
		}

		if (containsOrAlignsWith(r)) {
			return r;
		}

		if (r.containsOrAlignsWith(this)) {
			return this;
		}
		return null;
	}

	public Time getStartTime() {
		if (this.startTime == null) {
			this.startTime = Time.inNanos(this.start);
		}
		return this.startTime;
	}

	public Date getStartDate() {
		if (this.startDate == null) {
			this.startDate = new ImmutableDate(
					TimeUtil.nanosToMillis(this.start));
		}
		return this.startDate;
	}

	public long getEnd() {
		return this.end;
	}

	public Time getEndTime() {
		if (this.endTime == null) {
			this.endTime = Time.inNanos(this.end);
		}

		return this.endTime;
	}

	public Date getEndDate() {
		if (this.endDate == null) {
			this.endDate = new ImmutableDate(TimeUtil.nanosToMillis(this.end));
		}
		return this.endDate;
	}

	public long getDuration() {
		return this.end - this.start;
	}

	public long getDurationMillis() {
		return TimeUtil.nanosToMillis(getDuration());
	}

	public double getDurationFractionalMinutes() {
		return TimeUtil.nanosToFractionalMinutes(getDuration());
	}

	public long getDurationMinutes() {
		return TimeUtil.nanosToMinutes(getDuration());
	}

	public Granularity getDurationAsGranularity() {
		if (this.durationGranularity == null) {
			this.durationGranularity = Granularity.inNanos(this.end
					- this.start);
		}

		return this.durationGranularity;
	}
//factor increment
	public TimeRange augmentEvenly(double factor) {
		if ((factor < 0.0D) || (factor > 1.0D)) {
			throw new IllegalArgumentException("Factor must be between 0 and 1");
		}

		long fudgeOffset = (long) (getDuration() * factor / 2.0D);
		long fudgeStart = this.start - fudgeOffset;
		if (fudgeStart < 0L) {
			fudgeStart = 0L;
		}

		long fudgeEnd = this.end + fudgeOffset;
		return new TimeRange(fudgeStart, fudgeEnd);
	}

	public TimeRange augmentToAlignOn(Granularity gran) {
		long granNanos = gran.getNanos();
		long newStart = this.start / granNanos * granNanos;
		long newEnd = (this.end + granNanos - 1L) / granNanos * granNanos;
		return new TimeRange(newStart, newEnd);
	}

	public TimeRange augmentToOverlapWith(Granularity gran) {
		long offset = gran.getNanos() - 1L;
		long newStart = this.start - offset;
		if (newStart < 0L)
			newStart = 0L;
		return new TimeRange(newStart, this.end + offset);
	}

	public TimeRange shrinkToAlignOn(Granularity gran) {
		long granNanos = gran.getNanos();
		long newStart = (this.start + granNanos - 1L) / granNanos * granNanos;
		long newEnd = this.end / granNanos * granNanos;
		if (newEnd <= newStart) {
			return null;
		}
		return new TimeRange(newStart, newEnd);
	}

	public TimeRange advanceToAlignOn(Granularity gran) {
		long granNanos = gran.getNanos();
		long newStart = (this.start + granNanos - 1L) / granNanos * granNanos;
		long newEnd = (this.end + granNanos - 1L) / granNanos * granNanos;
		return new TimeRange(newStart, newEnd);
	}

	public TimeRange advanceEndToEnsureDuration(Granularity gran) {
		if (getDuration() >= gran.getNanos()) {
			return this;
		}

		return new TimeRange(this.start, this.start + gran.getNanos());
	}

	public TimeRange advance(long nanos) {
		return new TimeRange(this.start + nanos, this.end + nanos);
	}

	public TimeRange advance(Time time) {
		return new TimeRange(this.start + time.getNanos(), this.end
				+ time.getNanos());
	}

	public int getSegmentIndex(Time time, int numSegments) {
		return getSegmentIndex(time.getNanos(), numSegments);
	}

	public int getSegmentIndex(long nanos, int numSegments) {
		if (!contains(nanos)) {
			return -1;
		}
		long segmentWidth = getDuration() / numSegments;
		int res = (int) ((nanos - this.start) / segmentWidth);
		if (res == numSegments) {
			return numSegments - 1;
		}
		return res;
	}

	public TimeRange getSegment(int segNo, int numSegments) {
		if ((segNo < 0) || (segNo >= numSegments)) {
			throw new IllegalArgumentException("segNo[" + segNo
					+ "] is < 0 or >= numSegments[" + numSegments + "]");
		}
		long segmentWidth = getDuration() / numSegments;
		return new TimeRange(this.start + segmentWidth * segNo, this.start
				+ segmentWidth * segNo + 1);
	}

	public List<TimeRange> getSegments(int numSegments) {
		if (numSegments == 0) {
			return Collections.emptyList();
		}
		Granularity segmentSize = Granularity.inNanos(getDuration()
				/ numSegments);
		List<TimeRange> res = new ArrayList<TimeRange>(numSegments);
		Time startTime = getStartTime();
		TimeRange segmentRange = between(startTime, startTime.plus(segmentSize));
		for (int i = 0; i < numSegments; i++) {
			res.add(segmentRange);
			segmentRange = segmentRange.advance(segmentSize);
		}
		return res;
	}

	public List<TimeRange> getSegments(Granularity gran) {
		return getSegments((int) getNumSegments(gran));
	}

	public long getNumSegments(Granularity gran) {
		return getDuration() / gran.getNanos();
	}

	public Map<TimeRange, Collection<HasTimeStamp>> groupBySegment(
			Collection<HasTimeStamp> vals, final int numSegments) {
		Map<Integer, Collection<HasTimeStamp>> bySegmentId = ListUtil.groupBy(
				vals, new GroupingClosure<Integer, HasTimeStamp>()
				{
					public Integer getGroupFor(HasTimeStamp obj) 
					{
						return Integer.valueOf(TimeRange.this.getSegmentIndex(
								obj.getTimeStamp(),
								numSegments));
					}

				});
		Map<TimeRange, Collection<HasTimeStamp>> res = new HashMap<TimeRange, Collection<HasTimeStamp>>(
				bySegmentId.size());
		for (Entry<Integer, Collection<HasTimeStamp>> ent : bySegmentId
				.entrySet()) {
			int segNo = ((Integer) ent.getKey()).intValue();
			if (segNo != -1) {
				res.put(getSegment(segNo, numSegments), ent.getValue());
			}
		}
		return res;
	}

	public List<TimeRange> getAlignedRangesContainedWithin(Granularity gran) {
		List<TimeRange> res = new ArrayList<TimeRange>();
		Time start = getStartTime().alignUpToGranularity(gran);
		Time end = start.plus(gran);
		TimeRange subRange = between(start, end);
		while (containsOrAlignsWith(subRange)) {
			res.add(subRange);
			start = subRange.getEndTime();
			subRange = between(start, start.plus(gran));
		}
		return res;
	}

	public List<TimeRange> getAlignedRangesContaining(Granularity gran) {
		Time start = getStartTime().alignDownToGranularity(gran);
		Time end = getEndTime().alignUpToGranularity(gran);
		int numSegments = (int) (end.minus(start).getNanos() / gran.getNanos());
		List<TimeRange> res = new ArrayList<TimeRange>(numSegments);

		Time segmentStart = start;
		for (int i = 0; i < numSegments; i++) {
			Time segmentEnd = segmentStart.plus(gran);
			res.add(between(segmentStart, segmentEnd));
			segmentStart = segmentEnd;
		}

		return res;
	}

	public boolean alignsOnGranularityWindow(Granularity gran) {
		return (gran.getNanos() == getDuration())
				&& (Time.nanosAlignOnGranularity(this.start, gran));
	}

	public boolean alignsOnGranularity(Granularity gran) {
		return (Time.nanosAlignOnGranularity(this.start, gran))
				&& (Time.nanosAlignOnGranularity(this.end, gran));
	}

	public Granularity getEvenDivision(int numSegments) {
		Granularity gran = Granularity.inNanos(getDuration() / numSegments);
		return gran;
	}
//intersect percentage
	public double percentageWithin(TimeRange that) {
		if (that.containsOrAlignsWith(this)) {
			return 1.0D;
		}
		TimeRange intersect = intersect(that);
		if (intersect == null) {
			return 0.0D;
		}
		return intersect.getDuration() / getDuration();
	}

	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + (int) (this.end ^ this.end >>> 32);
		result = prime * result + (int) (this.start ^ this.start >>> 32);
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeRange other = (TimeRange) obj;
		if (this.end != other.end)
			return false;
		if (this.start != other.start)
			return false;
		return true;
	}

	public String toString() {
		if ((this.start < TimeUtil.secondsToNanos(600))
				&& (this.end < TimeUtil.secondsToNanos(600))) {
			return "[" + this.start + " -> " + this.end + "]";
		}
		return "[" + new Date(TimeUtil.nanosToMillis(this.start)) + " -> "
				+ new Date(TimeUtil.nanosToMillis(this.end)) + "]";
	}

	public static TimeRange fromContiguous(List<TimeRange> ranges) {
		if (ranges.isEmpty()) {
			throw new IllegalArgumentException("Must specify ranges");
		}

		if (!rangesAreContiguous(ranges)) {
			throw new IllegalArgumentException("Ranges not contiguous: "
					+ ranges);
		}

		return new TimeRange(ranges.get(0).getStart(),
				                                         ranges.get(ranges.size() - 1).getEnd());
	}

	public static boolean rangesAreContiguous(List<TimeRange> ranges) {
		if (ranges.isEmpty()) {
			return true;
		}

		Iterator<TimeRange> it = ranges.iterator();
		TimeRange first = it.next();
		long previousEnd = first.getEnd();
		while (it.hasNext()) {
			TimeRange cur = (TimeRange) it.next();
			if (cur.getStart() != previousEnd) {
				return false;
			}
			previousEnd = cur.getEnd();
		}
		return true;
	}

	public static TimeRange milliTimeRange(long startMillis, long endMillis) {
		return new TimeRange(TimeUtil.millisToNanos(startMillis),
				TimeUtil.millisToNanos(endMillis));
	}

	public static TimeRange timeRangeInSeconds(int startSeconds, int endSeconds) {
		return new TimeRange(TimeUtil.secondsToNanos(startSeconds),
				TimeUtil.secondsToNanos(endSeconds));
	}

	public static TimeRange nanoTimeRange(long startNanos, long endNanos) {
		return new TimeRange(startNanos, endNanos);
	}

	public static TimeRange range(long startNanos, long endNanos) {
		return nanoTimeRange(startNanos, endNanos);
	}

	public static TimeRange between(Time start, Time end) {
		return nanoTimeRange(start.getNanos(), end.getNanos());
	}

	public static boolean durationsAre(Collection<TimeRange> ranges,
			Granularity gran) {
		for (TimeRange r : ranges) {
			if (r.getDuration() != gran.getNanos()) {
				return false;
			}
		}
		return true;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(serialVersionUID);
		out.writeLong(this.start);
		out.writeLong(this.end);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		long version = in.readLong();
		if (version != serialVersionUID) {
			throw new IllegalStateException(
					"Unable to deserialize this TimeRange; unexpected serialVersionUID");
		}
		this.start = in.readLong();
		this.end = in.readLong();
	}

	public static class DescendingEndComparator implements
			Comparator<TimeRange> {
		public static final DescendingEndComparator INSTANCE = new DescendingEndComparator();

		public int compare(TimeRange o1, TimeRange o2) {
			return TimeRange.EndComparator.INSTANCE.compare(o2, o1);
		}
	}

	public static class EndComparator implements Comparator<TimeRange> {
		public static final EndComparator INSTANCE = new EndComparator();

		public int compare(TimeRange o1, TimeRange o2) {
			if (o1.getEnd() > o2.getEnd())
				return 1;
			if (o1.getEnd() < o2.getEnd()) {
				return -1;
			}
			return 0;
		}
	}
}