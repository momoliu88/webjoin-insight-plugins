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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.ebupt.webjoin.insight.util.MathUtil;

 
public class FrameId implements Serializable {
    private static final long serialVersionUID = 456584544836390297L;

    public static final int	QUICK_IDS_CACHE_SIZE=FrameBuilder.DEFAULT_MAX_FRAMES_PER_TRACE;
    // the 1st QUICK_IDS_CACHE_SIZE id(s) are cached by index in order to avoid creating Long values
    static final List<FrameId>	quickIds=new ArrayList<FrameId>(QUICK_IDS_CACHE_SIZE);
    // ID(s) larger than QUICK_IDS_CACHE_SIZE are stored in a map
    static final Map<Long,FrameId>	largeMap=new WeakHashMap<Long, FrameId>(FrameBuilder.DEFAULT_MAX_FRAMES_PER_TRACE);

    static {
    	// populate with null(s) to ensure size
    	for (int	index=0; index < QUICK_IDS_CACHE_SIZE; index++) {
    		quickIds.add(null);
    	}
    }

    /**
     * Compare 2 {@link FrameId}s accorindd to their {@link FrameId#getId()} values
     */
    public static final Comparator<FrameId> BY_ID_COMPARATOR=new Comparator<FrameId>() {
			public int compare(FrameId o1, FrameId o2) {
				if (o1 == o2) {
					return 0;
				}

				long	id1=(o1 == null) ? (-1L) : o1.getId();
				long	id2=(o2 == null) ? (-1L) : o2.getId();
				return MathUtil.signOf(id1 - id2); 
			}
		};

    private long id;

    /*
     * Constructor and Setter Methods package scope to allow for mutability in deserialization
     */
    FrameId() {
        super();
    }

	FrameId(long frameId) {
		this.id = frameId;
	}

	public static FrameId valueOf(String id) throws NumberFormatException {
	    return valueOf(Long.parseLong(id));
	}

    /*
     * This ensures that we keep using the same reference to after deserialization.
     */    
    public Object readResolve() throws ObjectStreamException {
    	if (id < 0L) {
    		throw new StreamCorruptedException("Negative values N/A: " + id);
    	}

    	return valueOf(id);
    }

	public static FrameId valueOf(long idValue) {
		if (idValue < 0L) {
			throw new NumberFormatException("Negative ID N/A: " + idValue);
		} else if (idValue < QUICK_IDS_CACHE_SIZE) {
			synchronized(quickIds) {
				int		index=(int) idValue;
				FrameId	frameId=quickIds.get(index);
				if (frameId == null) {
					frameId = new FrameId(idValue);
					quickIds.set(index, frameId);
				}

				return frameId;
			}
		} else {
			synchronized(largeMap) {
				Long	key=Long.valueOf(idValue);
				FrameId	frameId=largeMap.get(key);
				if (frameId == null) {
					frameId = new FrameId(idValue);
					largeMap.put(key, frameId);
				}

				return frameId;
			}
		}
	}

    public long getId() {
        return id;
    }

    void setId(long frameId) {
        this.id = frameId;
    }

    @Override
	public String toString() {
	    return Long.toString(getId());
	}
	
	@Override
	public int hashCode() {
		return MathUtil.hashValue(getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FrameId other = (FrameId) obj;
		return getId() == other.getId();
	}
}
