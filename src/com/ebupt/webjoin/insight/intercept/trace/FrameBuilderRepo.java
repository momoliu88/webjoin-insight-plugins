package com.ebupt.webjoin.insight.intercept.trace;

import java.util.WeakHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

public class FrameBuilderRepo {
	static WeakHashMap<Thread, FrameBuilder> frameBuilders = new WeakHashMap<Thread, FrameBuilder>();
    private static final Logger logger = Logger.getLogger(FrameBuilderRepo.class.getName());

	public static void put(Thread thread, FrameBuilder fb) {
		if(logger.isLoggable(Level.FINE))
			logger.log(Level.FINE,"Put into frameBuilders:"+thread+":"+fb);
		frameBuilders.put(thread, fb);
	}

	public static void del(Thread thread) {
		frameBuilders.remove(thread);
	}

	public static WeakHashMap<Thread, FrameBuilder> all() {
		return frameBuilders;
	}
}
