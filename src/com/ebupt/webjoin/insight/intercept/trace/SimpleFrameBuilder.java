package com.ebupt.webjoin.insight.intercept.trace;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.Insight;
import com.ebupt.webjoin.insight.InsightAgentPluginsHelper;
import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.collection.strategy.PostCollectionStrategyRunner;
import com.ebupt.webjoin.insight.color.ColorManager;
import com.ebupt.webjoin.insight.intercept.DelegatingFrameBuilderCallbacks;
import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.endpoint.EndPointAnalyzersRegistry;
import com.ebupt.webjoin.insight.intercept.endpoint.EndPointPopulator;
import com.ebupt.webjoin.insight.intercept.ltw.ClassLoaderUtils;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.util.time.NanoStopWatch;
import com.ebupt.webjoin.insight.intercept.util.time.StopWatch;
import com.ebupt.webjoin.insight.intercept.util.time.StopWatchFactory;
import com.ebupt.webjoin.insight.intercept.util.time.TimeUtil;
import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.ClassUtil;
import com.ebupt.webjoin.insight.util.MapUtil;
import com.ebupt.webjoin.insight.util.StringUtil;



/**
 * Creates a Frame hierarchy, using simple call-stack semantics.
 *
 * To create a frame stack, simply:
 *
 *   builder.enter(rootOp)
 *      builder.enter(nestedOp)
 *      builder.exit()
 *   stack = builder.exit()
 */
public class SimpleFrameBuilder implements FrameBuilder {
    private final StopWatchFactory watchFactory;

    private StopWatchFrame topLevelFrame;
    private StopWatchFrame workingFrame;
    private long frameCount;
    private StopWatch traceWatch;
    private FrameBuilderCallback callbacks;
    private Map<String, Object> hints;
    private boolean aborted;
    private boolean hasEndPointPopulator;
    private final InterceptConfiguration	config;
    private final Insight insight;
    private PostCollectionStrategyRunner runner = PostCollectionStrategyRunner.getInstance();
    private int depth;

    public SimpleFrameBuilder() {
        this(new NanoStopWatch.NanoStopWatchFactory(), new DelegatingFrameBuilderCallbacks());
    }

    public SimpleFrameBuilder(StopWatchFactory factory, FrameBuilderCallback callback) {
        watchFactory = factory;
        callbacks = callback;

        config = InterceptConfiguration.getInstance();

        InsightAgentPluginsHelper	helper=InsightAgentPluginsHelper.getRegisteredInsightAgentPluginsHelper();
        if (helper != null) {
        	insight = helper.getInsight();
        } else {
        	insight = config.getInsight().populateConfigIfNotConfigured();
        }
    }

    public void enter(Operation op) {
        // Handling Runaway Frame Stacks:
        // We have reached the maximum number of frames which can be added to the
        // builder. This could indicate a very serious problem with one of the plugins
        // or the frame builder itself. We destroy the entire framestack and pretend
        // nothing happened. This could result in orphaned frames, but that is better
        // than a memory leak or putting restrictions on where the frame root starts.
        //
        // We do the same thing if the ABORTED hint was set. Often these can be set
        // from aspects themselves, or more importantly the AdviceErrorHandlingAspect -
        // were we to keep the frame stack around we would have to wait until the max
        // frames has been reached before cutting the cord. Instead, if as aspect
        // derived from insight collections blows up the aborted hint is set and the
        // next time a frame enters the frame stack will be reset.
        if (aborted /*|| (frameCount >= MAX_FRAMES_PER_TRACE)*/) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Frame stack exceeded MAX_FRAMES_PER_TRACE limit or has been aborted " +
                            "limit: " + MAX_FRAMES_PER_TRACE + " frameCount: " + frameCount
                            + " aborted: " + aborted);
            setHint(HINT_ABORTED, Boolean.TRUE);

            // preserve the application name hint if available
            reset(HINT_APPNAME);
        }
        
        if (workingFrame == null) {
            depth ++;
            clearAndStartTraceWatch();
            callbacks.enterRootFrame();
            topLevelFrame = createAndEnterTopLevelFrame(op);
            workingFrame = topLevelFrame;
            findEndPoint(topLevelFrame);
        } else {
            depth ++;
            StopWatchFrame oldWorking = workingFrame;
            workingFrame = createAndEnterChildFrame(oldWorking, op);
            callbacks.enterChildFrame(workingFrame);
        }
        
    }

    private void findEndPoint(Frame frame) {
        if (!aborted && hasEndPointPopulator) {
        	ApplicationName	app=getHint(HINT_APPNAME, ApplicationName.class);
        	if ((app == null) || ApplicationName.UNKOWN_APPLICATION.equals(app)) {
        		ClassLoader	cl=ClassUtil.getDefaultClassLoader(getClass());
        		app = ClassLoaderUtils.findApplicationName(cl);
        		if (!ApplicationName.UNKOWN_APPLICATION.equals(app)) {
        			setHint(HINT_APPNAME, app);
        		}
        	}

        	if (insight.isContextIgnored(app)) {
        		return;	// don't spend any time on ignored applications
        	}

            EndPointAnalyzersRegistry   registry=EndPointAnalyzersRegistry.getInstance();
            registry.findEndPointAnalysis(this, frame, depth);
        }
    }

    private void clearAndStartTraceWatch() {
        long startNanos = TimeUtil.millisToNanos(System.currentTimeMillis());
        traceWatch = watchFactory.createWatch();
        traceWatch.start(startNanos);
    }

    private StopWatchFrame createAndEnterTopLevelFrame(Operation op) {
        StopWatchFrame res = new StopWatchFrame(allocateNewFrameId(), null, traceWatch);
        ApplicationName	appName = InterceptConfiguration.resolveCurrentApplicationName(this, getClass());
        if (!ApplicationName.UNKNOWN_APPLICATION_NAME.equals(appName)) {
            setHintIfRoot(FrameBuilder.HINT_APPNAME, appName);
        }

        res.enter(op);
        return res;
    }

    private StopWatchFrame createAndEnterChildFrame(Frame parent, Operation op) {
        StopWatchFrame res = new StopWatchFrame(allocateNewFrameId(), parent, traceWatch);
        res.enter(op);
        return res;
    }

    private FrameId allocateNewFrameId() {
        return FrameId.valueOf(frameCount++);
    }

    public Operation peek() {
    	Frame	frame=peekFrame();
    	if (frame == null) {
    		return null;
    	} else {
    		return frame.getOperation();
    	}
    }

    public Frame peekFrame () {
    	return workingFrame;
    }

    public Frame exit() {
        boolean exitingRoot = false;
        if (workingFrame == null) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,
                    "Attempted to exit a frame when none was in process");
            return null;
        }
        workingFrame.getOperation().finalizeConstruction();
        Frame oldWorkingFrame = workingFrame;
        workingFrame = (StopWatchFrame) workingFrame.getParent();
        
        ((StopWatchFrame)oldWorkingFrame).exit();
        if (workingFrame != null) {
            workingFrame.addChild(oldWorkingFrame);
        }
        
        runner.run(oldWorkingFrame);
        
        if (workingFrame == null) {
            exitingRoot = true;
        } else if (oldWorkingFrame.getParent() != null) {
        	callbacks.exitChildFrame(oldWorkingFrame);
        	findEndPoint(oldWorkingFrame);
        }
        
        depth--;

        if (exitingRoot) {
        	/*
        	 * Make sure the application name hint is set correctly
        	 */
        	ApplicationName	appName=InterceptConfiguration.resolveCurrentApplicationName(this, getClass());
           	Boolean discard = getHint(FrameBuilder.HINT_DISCARD, Boolean.class);
            if (hints == null) {
            	hints = Collections.<String,Object>singletonMap(FrameBuilder.HINT_APPNAME, appName);
            } else {
            	hints.put(HINT_APPNAME, appName);
            }

            if (insight.isContextIgnored(appName)
             || ((discard != null) && discard.booleanValue() && (!frameHasException(oldWorkingFrame)))) {
            	dump();
            	return null;
            }

            ColorManager	colorManager=ColorManager.getInstance();
            colorManager.setColor(oldWorkingFrame.getOperation());                
            callbacks.exitRootFrame(oldWorkingFrame, hints);
            hints = null;
        }

        return oldWorkingFrame;
    }
    
    public void discard(Frame frame) {
        if (!isFrameInTrace(frame)) {
            throw new IllegalArgumentException("Frame " + frame.getId() + " is not part of this trace " + topLevelFrame.getId());
        }
        
        //make sure we are not discarding frames that contains exceptions
        if (!frameHasException(frame)) {
            StopWatchFrame child = (StopWatchFrame) frame;
            StopWatchFrame parent = (StopWatchFrame)(frame.getParent());
            if (parent != null) {
                parent.discard(child);
            } else {
                //add discard hint in case the given frame is the root frame. We can't really discard root frames, instead
                //we set a hint on the trace so that it will not be dispatched (see exit())
                setHintIfRoot(FrameBuilder.HINT_DISCARD, Boolean.TRUE);
            }
        }
    }
    
    private boolean frameHasException(Frame frame) {
        Operation op = frame.getOperation();
        return op != null && op.get(OperationFields.EXCEPTION) != null;
    }

    public void dump() {
        reset(ArrayUtil.EMPTY_STRINGS);
    }

    private void reset(String ... preservedHints) {
        topLevelFrame = null;
        workingFrame = null;
        frameCount = 0L;
        traceWatch = null;

        if ((MapUtil.size(hints) > 0) && (ArrayUtil.length(preservedHints) > 0)) {
        	Map<String,Object>	oldHints=null;
        	for (String hintName : preservedHints) {
        		Object	hintValue=hints.get(hintName);
        		if (hintValue == null) {
        			continue;
        		}

        		if (oldHints == null) {
        			oldHints = new HashMap<String, Object>(hints.size());
        		}

        		oldHints.put(hintName, hintValue);
        	}

        	hints = oldHints;
        } else {
        	hints = null;
        }

        depth = 0;
    }


    public <T> T getHint(String hint, Class<T> type) {
		Object	value=getHint(hint);
		if (value == null) {
			return null;
		}

		if (type.isAssignableFrom(value.getClass())) {
			return type.cast(value);
		} else {
			return null;
		}
	}

    public Object getHint(String hint) {
        if (hints == null) {
            return null;
        } else {
        	return hints.get(hint);
        }
    }

    public void setHint(String hint, Object value) {
    	if (StringUtil.isEmpty(hint)) {
    		throw new IllegalArgumentException("No hint specified");
    	}

        if (hints == null) {
            hints = new HashMap<String, Object>();
        }
        
        if (hint.equals(FrameBuilder.HINT_ABORTED)) {
            aborted = Boolean.TRUE.equals(value);
        } else if (hint.equals(EndPointPopulator.HINT_NAME)) {
            hasEndPointPopulator = true;
        }

        if (value != null) {
        	hints.put(hint, value);
        } else {	// intrepret a null value as a deletion request
        	hints.remove(hint);
        }
    }

    public void setHintIfRoot(String hint, Object value) {
        if (workingFrame == topLevelFrame) {
            setHint(hint, value);
        }
    }

    /**
     * Used for testing
     */
    public long getFrameCount() {
        return frameCount;
    }

    public boolean isFrameInTrace(Frame frame) {
        return (frame == topLevelFrame) || (frame == workingFrame) || FrameUtil.frameIsAncestor(topLevelFrame, frame);
    }

	public boolean isCurrentRootFrame(Frame frame) {
		return (frame != null) && (topLevelFrame == frame);
	}

	@Override
	public int getdepth() {
		return depth;
	}

	@Override
	public Map<String, Object> getHints() {
		return this.hints;
	}
}