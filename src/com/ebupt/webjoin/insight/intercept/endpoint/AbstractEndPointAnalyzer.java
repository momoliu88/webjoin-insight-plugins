/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ebupt.webjoin.insight.intercept.endpoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.FrameUtil;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.util.ListUtil;
 

/**
 * Provides some useful default behavior for {@link EndPointAnalyzer} implementations
 */
public abstract class AbstractEndPointAnalyzer implements EndPointAnalyzer {
	private final List<OperationType>	ops;

	protected AbstractEndPointAnalyzer(Collection<OperationType> typesList) {
		ops = (ListUtil.size(typesList) <= 0)
			? Collections.<OperationType>emptyList()
			: Collections.unmodifiableList(new ArrayList<OperationType>(typesList))
			;
	}

	public final List<OperationType> getOperationTypes() {
		return ops;
	}
	
	// calls 'makeEndPoint' with the scoring frame (if not null)
	public EndPointAnalysis locateEndPoint(Trace trace) {
		Frame	frame=getScoringFrame(trace);
		if (frame == null) {
			return null;
		} else {
			return makeEndPoint(frame, FrameUtil.getDepth(frame));
		}
	}

	// calls 'makeEndPoint' if the frame is valid
	public EndPointAnalysis locateEndPoint(Frame frame, int depth) {
		OperationType	opType=validateScoringFrame(frame);
		if (opType == null) {
			return null;
		} else {
			return makeEndPoint(frame, depth);
		}
	}

	// if frame is valid then returns the depth, otherwise returns negative infinity
	public int getScore(Frame frame, int depth) {
		OperationType	opType=validateScoringFrame(frame);
		if (opType == null) {
			return EndPointAnalysis.MIN_SCORE_VALUE;
		}

		// check if the operation carries a score
		return getOperationScore(frame.getOperation(), depth);
	}

	// looks for FIRST frame whose type is one of the "getOperationTypes" and is a valid frame
	public Frame getScoringFrame(Trace trace) {
		for (OperationType type : getOperationTypes()) {
			Frame	frame=trace.getFirstFrameOfType(type);
			if (frame != null) {
				return frame;
			}
		}

		return null;	// no match
	}

    protected int getOperationScore(Operation op, int depth) {
        Number score=op.get(EndPointAnalysis.SCORE_FIELD, Number.class);
        if (score == null) {
        	return getDefaultScore(depth);
        } else {
        	return score.intValue();
        }
    }

    protected int getDefaultScore (int depth) {
    	return EndPointAnalysis.depth2score(depth);
    }
	/**
	 * Make sure that the provided {@link Frame} is a valid scoring frame by
	 * checking the following conditions:</BR>
	 * <UL>
	 * 		<LI>
	 * 		The frame carries one of the {@link OperationType)-s listed in
	 * 		the {@link #getOperationTypes()} return value
	 * 		</LI>
	 * 
	 * 		<LI>
	 * 		The frame has no parent of the same operation - i.e., it is the
	 * 		<U>first</U> frame of its type in the trace
	 * 		</LI>
	 * </UL
	 * @param frame The {@link Frame} to be validated
	 * @return The {@link OperationType} if valid - <code>null</code> otherwise
	 * @see #validateFrameParent(Frame, OperationType)
	 */
	protected OperationType validateScoringFrame (Frame frame) {
		OperationType	opType=validateFrameOperation(frame);
		if (opType == null) {	// not one of the valid frames
			return null;
		}

		if (validateFrameParent(frame, opType) != null) {
			return null;
		} else {
			return opType;
		}
	}

	/**
	 * Makes sure that the {@link Frame} does not have any ancestor with the
	 * specified {@link OperationType}
	 * @param frame The {@link Frame} to start the search from (exclusive)
	 * @param opType The excluded {@link OperationType}
	 * @return The first ancestor that has the excluded operation type
	 * - <code>null</code> if none found
	 */
	protected Frame validateFrameParent (Frame frame, OperationType opType) {
		return FrameUtil.getLastParentOfType(frame, opType);
	}

	protected OperationType validateFrameOperation (Frame frame) {
		return FrameUtil.validateFrameOperation(frame, getOperationTypes());
	}

	protected abstract EndPointAnalysis makeEndPoint (Frame frame, int depth);
}
