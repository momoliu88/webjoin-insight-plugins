package com.ebupt.webjoin.insight.intercept.trace;

import java.util.Collection;

/**
 * Locates {@link Frame}s from a {@link Trace} according to some criteria
 */
public interface TraceFramesLocator {
	/**
	 * @param trace The {@link Trace} to be analyzed
	 * @return A {@link Collection} of {@link Frame}-s representing matches
	 * according to some criteria (or empty if none found)
	 */
	Collection<Frame> locateFrames(Trace trace);
}
