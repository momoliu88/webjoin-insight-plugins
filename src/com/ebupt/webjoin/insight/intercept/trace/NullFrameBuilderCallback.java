package com.ebupt.webjoin.insight.intercept.trace;

import java.util.Map;

public abstract class NullFrameBuilderCallback implements FrameBuilderCallback {
	public void enterChildFrame(Frame frame) {
	}

	public void exitChildFrame(Frame frame) {
	}

	public void enterRootFrame() {
	}

	public void exitRootFrame(Frame frame, Map<String, Object> hints) {
	}
}