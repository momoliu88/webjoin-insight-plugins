package com.ebupt.webjoin.insight.collection;

import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.intercept.trace.ObscuredValueMarker;
import com.ebupt.webjoin.insight.intercept.trace.ObscuredValueRegistry;
import com.ebupt.webjoin.insight.intercept.trace.SimpleObscuredValueRegistry;

public class FrameBuilderHintObscuredValueMarker implements ObscuredValueMarker {
	private final FrameBuilder builder;

	public FrameBuilderHintObscuredValueMarker(FrameBuilder builder) {
		this.builder = builder;
	}
	@Override
	public void markObscured(Object o) {
		ObscuredValueRegistry registry = (ObscuredValueRegistry) this.builder
				.getHint(FrameBuilder.HINT_OBSCURED_REGISTRY);
		if (registry == null) {
			registry = new SimpleObscuredValueRegistry();
			this.builder.setHint(FrameBuilder.HINT_OBSCURED_REGISTRY, registry);
		}
		registry.markObscured(o);
	}
}