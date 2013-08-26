package com.ebupt.webjoin.insight.intercept;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilderRepo;
import java.util.Date;
public class InterceptTimerTask extends TimerTask{

	static Long MAX_TRACINGTIME = 60L*1000*1000000;//10s
    private static final Logger logger = Logger.getLogger(InterceptTimerTask.class.getName());

	@Override
	public void run() {
		logger.log(Level.FINE,"["+new Date(System.currentTimeMillis())+"]:running timer task...");
		Map<Thread,FrameBuilder> activeBuilders = FrameBuilderRepo.all();
		for(Entry<Thread,FrameBuilder> activeBuilder:activeBuilders.entrySet()){
			FrameBuilder builder = activeBuilder.getValue() ;
			if(builder == null||builder.peekFrame() == null) continue;
			Long topLevelTimestamp = builder.peekFrame().getStart().getNanos();
			Long systemCurrentTimeStamp = System.nanoTime();
			if((systemCurrentTimeStamp - topLevelTimestamp) > MAX_TRACINGTIME){
				// exceed max tracing time
				//need to pop all things from stack
				logger.log(Level.WARNING,"["+activeBuilder.getKey()+"]:exceed max tracing time");
				builder.setHint(FrameBuilder.HINT_EXCEED_MAX_TRACINGTIME, Integer.valueOf(1));
				reset(builder);
			}
		}
	}
	private void reset(FrameBuilder builder){
		while(builder.peekFrame() != null)
		{
			builder.exit();
		}
		builder.setHint(FrameBuilder.HINT_DISCARD, true);
	}

}
