package com.ebupt.webjoin.insight.intercept.trace;

import java.io.Serializable;
import java.util.UUID;

public class TraceId implements Serializable {
	private static final long serialVersionUID = -3024115866171814535L;
	private String id;

	private TraceId() {
	}

	private TraceId(String id) {
		if (id == null) {
			throw new NullPointerException("new TraceId(null) called");
		}
		this.id = id;
	}

	public static TraceId valueOf() {
		return new TraceId(UUID.randomUUID().toString());
	}

	public String getId() {
		return this.id;
	}

	public String toString() {
		return this.id;
	}

	public int hashCode() {
		return this.id.hashCode();
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TraceId other = (TraceId) obj;
		return this.id.equals(other.id);
	}
}