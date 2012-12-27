package com.ebupt.webjoin.insight.color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.intercept.util.PercentageTracker;
import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

 

public final class ColorManager implements CollectionSettingsUpdateListener {
    private static final Logger logger = Logger.getLogger(ColorManager.class.getName());
    
    private static final class LazyFieldHolder {
        static final ColorManager INSTANCE = new ColorManager();
    }
    
    /**
     * Default tracing rate: 10%
     */
    public static final int DEFAULT_TRACING_RATE = 10;
    
    public static final CollectionSettingName CS_NAME =
            new CollectionSettingName("sampling-percentage", "cross-server-tracing");
    
    private static final InterceptConfiguration interceptConfig = InterceptConfiguration.getInstance();
    private static final CollectionSettingsRegistry registry = CollectionSettingsRegistry.getInstance();
    
    private final String agentId;
    private final AtomicLong colorId;
    private final PercentageTracker tracker;
    
    ColorManager() {
        this(interceptConfig.getServer().makeKey().getKey());
    }
    
    /* visibility for unit testing */ 
    ColorManager(String agentIdentifier) {
        this.agentId = agentIdentifier;
        this.colorId = new AtomicLong(0L);
        
        int registeredValue = getRegisteredTracingRate();
        this.tracker = new PercentageTracker("color-manager-tracker", registeredValue);
        
        registry.addListener(this);
    }
    
    public static int getRegisteredTracingRate() {
        Serializable value = registry.get(CS_NAME);
        
        if (value != null) {
            try {
                return CollectionSettingsRegistry.getIntegerSettingValue(value);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Invalid tracing rate (" + value + ") found in registry. Registering a new value " +
                		" (" + DEFAULT_TRACING_RATE + ") instead", e);
                
            }
        }
        
        registry.register(CS_NAME, Integer.valueOf(DEFAULT_TRACING_RATE));
        return DEFAULT_TRACING_RATE;
    }
    
    public static final ColorManager getInstance() {
        return LazyFieldHolder.INSTANCE;
    }

    public void incrementalUpdate(CollectionSettingName name, Serializable value) {
        if (CS_NAME.equals(name) && value != null) {
            this.tracker.setTrackedRatio(CollectionSettingsRegistry.getIntegerSettingValue(value));
            this.tracker.reset();
        }
    }

    /**
     * Used by plugins to get the current color
     *
     * @return
     */
    public void colorForward(ColorParams params) {
        List<Color> colors = getTraceColor();
        
        Color orgColor = colors != null ? colors.get(0) : null;
        Color color = createNewColor(orgColor, orgColor == null);
        
        if (color != null) {
            Color res = new Color(color.getInId(), createNewOutId(), agentId, color.getSourceId());
            String colorStr = res.toString();
            try {
                params.setColor(Color.TOKEN_NAME, colorStr);
                params.getOperation().put(Color.TOKEN_NAME, colorStr);
            } catch (Exception ex) {
                //Nothing to do, we can't color forward
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Color> getTraceColor() {
    	FrameBuilder	builder=interceptConfig.getFrameBuilder();
        return builder.getHint(Color.TOKEN_NAME, List.class);
    }

    public void extractColor(ExtractColorParams params) {
        String previousColor = params.getColor(Color.TOKEN_NAME);
        Color previous = null;
        Color color = null;
        
        if (previousColor != null) {
            previous = Color.valueOf(previousColor);
            
            if (previous != null) {
                color = new Color(previous.getOutId(), null, agentId, previous.getSourceId());
            }
        }
        
        createNewColor(color, true);
    }
    
    @SuppressWarnings("unchecked")
    private Color createNewColor(Color orgColor, boolean populate) {
        Color color=orgColor;
        color = generateNewColorIfNeeded(color);
        
        if (populate && (color != null)) {
            List<Color> inColors = interceptConfig.getFrameBuilder().getHint(Color.TOKEN_NAME, List.class);
            
            if (inColors == null) {
                inColors = new ArrayList<Color>();
                inColors.add(color);
                interceptConfig.getFrameBuilder().setHint(Color.TOKEN_NAME, inColors);
            } else {
                Color firstColor = inColors.get(0);
                
                if (color != firstColor) {
                    if (firstColor.getInId() != null) {
                        firstColor = new Color(null, null, agentId, createNewSourceId());
                        inColors.add(0, firstColor);
                    }
                    
                    inColors.add(color);
                    return firstColor;
                }
            }
        }
        
        return color;
    }
    
    /**
     * Generate a new color if <code>color</color> is <code>null</null> and <code>tracing threshold met</code>
     * 
     * @param color source color
     * @return new color if needed otherwise <code>null</code>
     */
    /* visibility for unit testing */ 
    Color generateNewColorIfNeeded(Color color) {
        if ((color == null) && tracker.isValueProcessingAllowed()) {
            return new Color(null, null, agentId, createNewSourceId());
        }
        
        return color;
    }

    @SuppressWarnings("unchecked")
    public List<Color> getColor(Map<String, Object> hints) {
        return (List<Color>) hints.get(Color.TOKEN_NAME);
    }

    public String getColor(Operation op) {
    	String color=(op == null) ? null : op.get(Color.TOKEN_NAME, String.class);
    	if (color != null) {
    		return color;
        } else {
        	return null;
        }
    }

    public void setColor(Operation op) {
        List<Color> colors=getTraceColor();
        String		value=toString(colors);
        if (!StringUtil.isEmpty(value)) {
            op.put(Color.TOKEN_NAME, toString(colors));
        }
    }
    
    public static String toString(Collection<? extends Color> colors) {
    	if (ListUtil.size(colors) <= 0) {
    		return null;
    	}

        StringBuilder builder = new StringBuilder(colors.size() * 16);
        for(Color clr : colors) {
        	if (builder.length() > 0) {
        		builder.append('*');
        	}
                
        	builder.append(clr.toString());
        }
            
        return builder.toString();
    }

    public static List<Color> valueOf(Operation op) {
        return valueOf(op.get(Color.TOKEN_NAME, String.class));
    }
    
    public static List<Color> valueOf(String str) {
        if (StringUtil.isEmpty(str)) {
        	return null;
        }

        String[] parts = str.split("[*]");
        List<Color> colors = new ArrayList<Color>(ArrayUtil.length(parts));
        for(String clrStr : parts) {
        	Color color = Color.valueOf(clrStr);
                
        	if (color != null) {
        		colors.add(color);
        	}
        }
            
        return colors;
    }
    
    public CollectionSettingName getCollectionSettingsName() {
		return CS_NAME;
	}

    private String createNewOutId() {
        return new StringBuilder(agentId)
                    .append('-')
                    .append(colorId.incrementAndGet())
                    .toString();
    }
    
    private String createNewSourceId() {
        return createNewOutId();
    }
    
    public interface ColorParams {

        void setColor(String key, String value);

        Operation getOperation();
    }
    
    public interface ExtractColorParams {

        String getColor(String key);
    }
}
