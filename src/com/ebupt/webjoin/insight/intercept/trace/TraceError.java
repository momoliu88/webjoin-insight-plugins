package com.ebupt.webjoin.insight.intercept.trace;

import java.io.Serializable;

public class TraceError implements Serializable {
	private String message;
	private static final long serialVersionUID = 488597078974014936L;

	@SuppressWarnings("unused")
	private TraceError() {
	}

	public TraceError(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}
}