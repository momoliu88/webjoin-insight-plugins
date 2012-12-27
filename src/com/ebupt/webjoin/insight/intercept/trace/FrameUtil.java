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

package com.ebupt.webjoin.insight.intercept.trace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.util.time.SelfTimeRange;
import com.ebupt.webjoin.insight.intercept.util.time.TimeRange;
import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.ListUtil;

 

public final class FrameUtil {
	private FrameUtil () {
		throw new UnsupportedOperationException("No instance");
	}

    /**
     * Get the depth of the frame in whatever stack it is in.
     * @param root The root {@link Frame} to start from and go <U>up</U>
     * @return 0 if the frame has no parent, else the # of ancestors
     * that a frame has.
     */
    public static int getDepth(Frame root) {
        int depth = 0;
        Frame   frame = root.getParent();
        while (frame != null) {
           depth++;
           frame = frame.getParent();
        }
        return depth;
    }
    
    /**
     * Returns true if the ancestor is an ancestor of the descendant.
     */
    public static boolean frameIsAncestor(Frame ancestor, Frame child) {
    	if ((ancestor == null) || (child == null)) {
    		return false;
    	}

        for (Frame   descendant = child.getParent(); descendant != null;  descendant = descendant.getParent()) {
            if (descendant == ancestor) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Creates the self time for the Frame
     */
    public static SelfTimeRange selfTime(Frame frame){
        return selfTime(frame.getRange(), frame.getChildren());
    }
    
    /**
     * Creates the self time for the Frame using it's TimeRange and child frames.
     */
    public static SelfTimeRange selfTime(TimeRange range, List<Frame> children){
        List<TimeRange> childTimeRanges = new ArrayList<TimeRange>();
        for (Frame child : children) {
            childTimeRanges.add(child.getRange());
        }
        return new SelfTimeRange(range, childTimeRanges);
    }
    
    /**
     * Set of {@link FrameId}s for each {@link Frame} in the collection
     */
    public static Set<FrameId> frameIDs(Collection<Frame> frames) {
        Set<FrameId> ids = new HashSet<FrameId>();
        for (Frame frame : frames) {
            ids.add(frame.getId());
        }
        return ids;
    }
    
    /**
     * Set of all {@link Frame}s that are descended from the frame matching the 
     * type.  The provided frame is not inspected.
     */
    public static Set<Frame> descendantFramesOfType(Frame frame, OperationType type) {
        Set<Frame> frames = new HashSet<Frame>();
        for (Frame child : frame.getChildren()) {
            if (child.getOperation().getType().equals(type)) {
                frames.add(child);
            }
            frames.addAll(descendantFramesOfType(child, type));
        }
        return frames;
    }
    
    /**
     * Set of all top-level {@link Frame}s matching the type.  The children of 
     * a matching frame are not inspected.
     */
    public static Set<Frame> topLevelFramesOfType(Frame frame, OperationType type) {
        Set<Frame> frames = new HashSet<Frame>();
        if (frame.getOperation().getType().equals(type)) {
            frames.add(frame);
        }
        else {
            for (Frame child : frame.getChildren()) {
                frames.addAll(topLevelFramesOfType(child, type));
            }
        }
        return frames;
    }

    /**
     * Traverses a {@link Frame}-s hierarchy
     * @param frame The root {@link Frame} to start traversal 
     * @param childrenFirst If <code>true</code> then the children are visited before
     * the frame that &quot;owns&quot; them
     * @param callback The {@link FrameTraverseCallback} to invoke for each frame
     * @return <code>true</code> if all frames visited, <code>false</code> if
     * traversal was stopped by the callback
     * @see FrameTraverseCallback#frameVisited(Frame)
     */
    public static boolean traverseFrameHierarchy (Frame frame, boolean childrenFirst, FrameTraverseCallback callback) {
    	if (!childrenFirst) {
            if (!callback.frameVisited(frame)) {
            	return false;
            }
    	}

    	Collection<? extends Frame>	children=frame.getChildren();
    	if (ListUtil.size(children) > 0) {
    		for (Frame child : children) {
    			if (!traverseFrameHierarchy(child, childrenFirst, callback)) {
    				return false;
    			}
    		}
    	}

    	if (childrenFirst) {
            if (!callback.frameVisited(frame)) {
            	return false;
            }
    	}

        return true;
    }

    /**
     * @param frame The {@link Frame} from which to start - ignored if
     * <code>null</code>
     * @param type The required {@link OperationType}
     * @return The first <U>ancestor</U> of the frame whose operation type
     * matches the required one - <code>null</code> if no match found
     */
    public static Frame getFirstParentOfType(Frame frame, OperationType type) {
    	if (frame == null) {
    		return null;
    	}

        for (Frame parent=frame.getParent(); parent != null; parent = parent.getParent()) {
        	Operation	op=parent.getOperation();
        	if (type.equals(op.getType())) {
        		return parent;
        	}
        }
         
        return null;
    }

    /**
     * Adds to the provided output list all the frame(s) that have the required
     * operation type but none of their descendants have it
     * @param opType Required {@link OperationType}
     * @param rootFrame Root {@link Frame} to start search
     * @param frames Output {@link List} of matching frames
     * @return <code>true</code> if either the root or one of its descendants
     * contained the required operation type
     */
    public static boolean getLastFramesOfType(OperationType opType, Frame rootFrame, List<Frame> frames) {
        if (rootFrame == null) {
            return false;
        }

        boolean haveChildWithType=false;
        for (Frame child : rootFrame.getChildren()) {
            if (getLastFramesOfType(opType, child, frames)) {
            	haveChildWithType = true;
            }
        }

        if (haveChildWithType) {
            return true;
        }

        Operation		op=rootFrame.getOperation();
        OperationType	type=op.getType();
        if (opType.equals(type)) {
            frames.add(rootFrame);
            return true;
        }

        return false;
    }

    public static Frame getLastParentOfType(Frame frame, OperationType type) {
    	if ((frame == null) || (type == null)) {
    		return null;
    	}

    	for (Frame parent=frame.getParent(); parent != null; parent=parent.getParent()) {
        	Operation	opParent=parent.getOperation();
            if (type.equals(opParent.getType())) {
                return parent;
            }
        }
         
        return null;
    }
    
    public static Frame getRoot(final Frame frame) {
        for (Frame   root=frame; root != null; root=root.getParent()) {
            if (root.isRoot()) {
                return root;
            }
        }
        
        return null;
    }

    public static Collection<Frame> getAllFramesOfType(Frame root, OperationType opType) {
        return getAllFramesOfType(root, opType, new LinkedList<Frame>());
    }

    public static <C extends Collection<Frame>> C getAllFramesOfType (
            final Frame rootFrame, final OperationType opType, final C framesList) {
        FrameUtil.traverseFrameHierarchy(rootFrame, true, new FrameTraverseCallback() {
                public boolean frameVisited(Frame frame) {
                    Operation       op=frame.getOperation();
                    OperationType   type=op.getType();
                    if (opType.equals(type)) {
                        framesList.add(frame);
                    }

                    return true;
                }
            });
        return framesList;
    }

    // "shortcut" for one operation
    public static OperationType validateFrameOperation (Frame frame, OperationType type) {
    	if ((frame == null) || (type == null)) {
    		return null;
    	}

    	Operation		op=frame.getOperation();
    	OperationType	opType=op.getType();
    	if (type.equals(opType)) {
    		return type;
    	} else {
    		return null;
    	}
	}

    public static OperationType validateFrameOperation (Frame frame, OperationType ... ops) {
    	if ((frame == null) || (ArrayUtil.length(ops) <= 0)) {
    		return null;
    	} else {
    		return validateFrameOperation(frame, Arrays.asList(ops));
    	}
    }

    public static OperationType validateFrameOperation (Frame frame, Collection<OperationType> ops) {
    	if ((frame == null) || (ListUtil.size(ops) <= 0)) {
    		return null;
    	}

    	Operation		op=frame.getOperation();
    	OperationType	opType=op.getType();
    	if (ops.contains(opType)) {
    		return opType;
    	} else {
    		return null;
    	}
    }

}
